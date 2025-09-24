package org.cabbage.shortlink.admin.common.enums;


import org.cabbage.shortlink.common.convention.errorcode.IErrorCode;

/**
 * @author xzcabbage
 * @since 2025/9/7
 */
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B00200", "用户记录不存在"),
    USER_NAME_EXIST("B00201", "用户名已存在"),
    USER_EXIST("B00202", "用户记录已存在"),
    USER_SAVE_ERROR("B00203", "用户记录新增失败"),
    USER_PASSWORD_ERROR("B00204", "用户密码错误"),
    USER_TOKEN_ERROR("B00205", "用户未登录或登录状态过期"),
    USER_ALREADY_LOGIN("B00206", "用户已登录");

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
