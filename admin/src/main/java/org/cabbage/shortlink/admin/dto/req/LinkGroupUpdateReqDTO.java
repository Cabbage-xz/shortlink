package org.cabbage.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接分组更新请求
 */
@Data
public class LinkGroupUpdateReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 新分组名
     */
    private String name;
}
