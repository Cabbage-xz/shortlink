package org.cabbage.shortlink.project.common.enums;

import org.cabbage.shortlink.common.convention.errorcode.IErrorCode;

/**
 * @author xzcabbage
 * @since 2025/9/25
 */
public enum ShortLInkErrorCodeEnum implements IErrorCode {


    SHORT_LINK_CREATE_TIMES_TOO_MANY("B003001", "短链接频繁生成，请稍后再试"),
    SHORT_LINK_ALREADY_EXIST("B003002", "短链接已存在"),
    SHORT_LINK_NOT_EXIST("B003003", "短链接不存在"),
    SHORT_LINK_GET_REMOTE_LOCALE_ERROR("B003004", "获取地理位置失败"),
    SHORT_LINK_GET_WRITE_LOCK_ERROR("B003005", "获取写锁失败"),
    SHORT_LINK_ANALYSE_ERROR("A003006", "填写原始链接有误"),
    SHORT_LINK_PROTECT_ERROR("A003007", "原始链接环境异常，请修改");

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
