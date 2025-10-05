package org.cabbage.shortlink.common.constant;

/**
 * @author xzcabbage
 * @since 2025/9/14
 * Redis缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册
     */
    public static final String LOCK_USER_REGISTER_KEY = "short-link:lock_user-register:";

    /**
     * 用户登录
     */
    public static final String LOCK_USER_LOGIN_KEY = "short-link:lock_user-login:";

    /**
     * 短链接跳转
     * %s 表示域名+短链接
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:link_goto_%s";

    /**
     * 短链接修改分组 ID 锁前缀 Key
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link_lock_update-gid_%s";

    /**
     * 短链接空值跳转
     * %s 表示域名+短链接
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:link_goto_%s";

    /**
     * 短链接跳转锁前缀
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock_link_goto_%s";

    /**
     * 短链接延迟队列消费统计 Key
     */
    public static final String DELAY_QUEUE_STATS_KEY = "short-link_delay-queue:stats";
}
