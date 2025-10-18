package org.cabbage.shortlink.project.initialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author xzcabbage
 * @since 2025/10/18
 * 初始化短链接监控队列消费者组
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShortLinkStatsStreamInitializeTask implements InitializingBean {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.data.redis.channel-topic.short-link-stats}")
    private String topic;

    @Value("${spring.data.redis.channel-topic.short-link-stats-group}")
    private String group;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            Boolean hasKey = stringRedisTemplate.hasKey(topic);

            if (Boolean.FALSE.equals(hasKey)) {
                // Stream 不存在，创建消费者组（会自动创建 Stream）
                log.info("Stream [{}] 不存在，创建消费者组 [{}]", topic, group);
                stringRedisTemplate.opsForStream().createGroup(topic, group);
                log.info("消费者组创建成功");
            } else {
                // Stream 存在，检查消费者组是否存在
                try {
                    StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream()
                            .groups(topic);

                    boolean groupExists = groups.stream()
                            .anyMatch(g -> group.equals(g.groupName()));

                    if (!groupExists) {
                        log.info("消费者组 [{}] 不存在，开始创建", group);
                        stringRedisTemplate.opsForStream()
                                .createGroup(topic, ReadOffset.from("0"), group);
                        log.info("消费者组创建成功");
                    } else {
                        log.info("Stream [{}] 和消费者组 [{}] 已存在", topic, group);
                    }
                } catch (Exception e) {
                    // 可能是权限问题或其他异常
                    log.error("检查消费者组失败", e);
                }
            }
        } catch (Exception e) {
            log.error("初始化 Redis Stream 失败", e);
            throw e;  // 抛出异常，阻止应用启动
        }
    }
}
