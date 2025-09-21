package org.cabbage.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接排序请求
 */
@Data
public class LinkGroupSortReqDTO {

    /**
     * 分组标识id
     */
    private String gid;

    /**
     * 排序
     */
    private Integer sortOrder;
}
