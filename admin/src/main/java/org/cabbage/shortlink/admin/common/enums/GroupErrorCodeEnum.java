package org.cabbage.shortlink.admin.common.enums;

import org.cabbage.shortlink.common.convention.errorcode.IErrorCode;

/**
 * @author xzcabbage
 * @since 2025/10/3
 */
public enum GroupErrorCodeEnum implements IErrorCode {

    GROUP_NOT_EXISTS("B00401", "短链接分组不存在"),
    GROUP_NUM_TOO_MANY("A00401", "短链接分组数量超过上限");

    private final String code;

    private final String message;

    GroupErrorCodeEnum(String code, String message) {
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
