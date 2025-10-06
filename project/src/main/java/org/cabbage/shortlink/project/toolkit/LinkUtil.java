package org.cabbage.shortlink.project.toolkit;

import cn.hutool.core.util.StrUtil;

import java.net.URI;
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

    /**
     * 获取原始链接中域名，若存在 www. 则将其移除
     * @param originalUrl 原始域名链接
     * @return 更新后的域名
     */
    public static String extractDomain(String originalUrl) {
        try {
            URI uri = new URI(originalUrl);
            String host = uri.getHost();
            if (StrUtil.isBlank(host)) {
                return null;
            }

            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception ignored) {
            return null;
        }
    }
}
