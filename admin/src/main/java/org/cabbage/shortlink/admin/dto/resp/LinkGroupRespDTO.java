package org.cabbage.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 分组返回实体
 */
@Data
public class LinkGroupRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
