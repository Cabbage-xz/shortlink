package org.cabbage.shortlink.project.mq.consumer;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.exception.ServiceException;
import org.cabbage.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.cabbage.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.DELAY_QUEUE_STATS_KEY;

/**
 * @author xzcabbage
 * @since 2025/10/6
 */
@Component
@RequiredArgsConstructor
@Deprecated
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    public void onMessage() {
        Executors.newSingleThreadExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_stats_consumer");
                            thread.setDaemon(Boolean.TRUE);
                            return thread;
                        })
                .execute(() -> {
                    RBlockingDeque<ShortLinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
                    RDelayedQueue<ShortLinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
                    for (; ; ) {
                        try {
                            ShortLinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            if (statsRecord != null) {
                                if (!messageQueueIdempotentHandler.isMessageProcessed(statsRecord.getMessageKeys())) {
                                    if (messageQueueIdempotentHandler.isMessageAccomplished(statsRecord.getMessageKeys())) {
                                        return;
                                    }
                                    throw new ServiceException("Message processing failed");
                                }
                                try {
                                    shortLinkService.shortLinkStats(null, null, statsRecord);
                                } catch (Throwable ex) {
                                    messageQueueIdempotentHandler.delMessageProcessedKey(statsRecord.getMessageKeys());
                                }
                                messageQueueIdempotentHandler.setMessageAccomplished(statsRecord.getMessageKeys());
                                continue;
                            }
                            LockSupport.parkUntil(500);
                        } catch (Throwable ignored) {
                        }
                    }
                });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}
