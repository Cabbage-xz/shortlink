package org.cabbage.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/9/20
 */
@Data
public class UserLoginReqDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
