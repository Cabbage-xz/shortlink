package org.cabbage.shortlink.project.mq.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author xzcabbage
 * @since 2025/10/18
 */
@Component
@RequiredArgsConstructor
public class MessageQueueIdempotentHandler {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENT_KEY_PREFIX = "short-link:idempotent";

    /**
     * 判断当前消息是否已经被消费过 设置值为0表示正在执行中
     * @param messageId 消息唯一标识
     * @return 是否被消费过 true-第一次被消费 false-已经被消费过
     */
    public boolean isMessageProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + ":" + messageId;
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, "0", 2, TimeUnit.MINUTES));
    }

    /**
     * 判断该消息是否成功执行完成
     * @param messageId 消息唯一标识
     * @return 是否执行完成
     */
    public boolean isMessageAccomplished(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + ":" + messageId;
        return Objects.equals(stringRedisTemplate.opsForValue().get(key), "1");
    }

    /**
     * 设置消息执行完成
     * @param messageId 消息唯一标识
     */
    public void setMessageAccomplished(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, "1");
    }

    /**
     * 删除消息被消费过的id
     * @param messageId 消息唯一标识
     */
    public void delMessageProcessedKey(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + ":" + messageId;
        stringRedisTemplate.delete(key);
    }
}
