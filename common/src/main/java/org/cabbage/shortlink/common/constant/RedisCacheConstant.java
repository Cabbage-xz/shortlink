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
     * 短链接跳转锁前缀
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock_link_goto_%s";
}
