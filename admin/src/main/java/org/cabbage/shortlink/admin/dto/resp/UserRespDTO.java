package org.cabbage.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/6/9
 */
@Data
public class UserRespDTO {

    /**
     * ID
     */
    private Long id;

    /*
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

}
