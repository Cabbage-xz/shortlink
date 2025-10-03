package org.cabbage.shortlink.common.constant;

/**
 * @author xzcabbage
 * @since 2025/10/2
 * 短链接常量类
 */
public class ShortLinkConstant {

    /**
     * 永久短链接默认缓存有效时间
     */
    public static final long DEFAULT_CACHE_VALID_TIME = 262656000L;

    /**
     * 短链接监控uv前缀
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "short:link:stats:uv:";

    /**
     * 短链接监控uip前缀
     */
    public static final String SHORT_LINK_STATS_UIP_KEY = "short:link:stats:uip:";
}
