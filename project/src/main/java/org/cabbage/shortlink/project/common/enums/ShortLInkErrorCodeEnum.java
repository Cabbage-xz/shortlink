package org.cabbage.shortlink.project.common.enums;

import org.cabbage.shortlink.common.convention.errorcode.IErrorCode;

/**
 * @author xzcabbage
 * @since 2025/9/25
 */
public enum ShortLInkErrorCodeEnum implements IErrorCode {


    SHORT_LINK_CREATE_TIMES_TOO_MANY("B003001", "短链接频繁生成，请稍后再试"),
    SHORT_LINK_ALREADY_EXIST("B003002", "短链接已存在");

    private final String code;

    private final String message;

    ShortLInkErrorCodeEnum(String code, String message) {
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
