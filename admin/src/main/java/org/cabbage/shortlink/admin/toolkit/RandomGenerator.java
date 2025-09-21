package org.cabbage.shortlink.admin.toolkit;

import java.security.SecureRandom;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 分组ID随机生成器
 */
public final class RandomGenerator {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成随机分组id
     *
     * @return 分组id
     */
    public static String generateRandom() {
        return generateRandom(6);
    }

    /**
     * 生成随机分组id
     *
     * @param length 长度
     * @return 分组id
     */
    public static String generateRandom(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
