package org.cabbage.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接分组请求
 */
@Data
public class LinkGroupAddReqDTO {

    /**
     * 用户分组名
     */
    private String name;
}
