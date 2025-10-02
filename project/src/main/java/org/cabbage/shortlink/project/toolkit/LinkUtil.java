package org.cabbage.shortlink.project.toolkit;

import java.time.Duration;
import java.time.LocalDateTime;

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
        if (validDate == null) {
            return DEFAULT_CACHE_VALID_TIME;
        }

        // 定义永久有效期的标识时间（2099-12-31 23:59:59）
        LocalDateTime permanentValidDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        // 如果是永久有效期，使用默认缓存时间
        if (validDate.isEqual(permanentValidDate) || validDate.isAfter(permanentValidDate)) {
            return DEFAULT_CACHE_VALID_TIME;
        }

        return Duration.between(LocalDateTime.now(), validDate).toMillis();
    }
}
