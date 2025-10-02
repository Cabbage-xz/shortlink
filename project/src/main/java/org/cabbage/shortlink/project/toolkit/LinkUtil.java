package org.cabbage.shortlink.project.toolkit;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.cabbage.shortlink.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * @author xzcabbage
 * @since 2025/10/2
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 依据传入有效期设置缓存有效期
     * @param validDate 用户传入有效期
     * @return 缓存有效期
     */
    public static long getLinkCacheValidTime(LocalDateTime validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> Duration.between(LocalDateTime.now(), each).toMillis())
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
}
