package org.cabbage.shortlink.admin.common.enums;

import org.cabbage.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * @author xzcabbage
 * @since 2025/9/7
 */
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B00200", "用户记录不存在"),
    USER_EXIST("B00201", "用户记录已存在");

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
