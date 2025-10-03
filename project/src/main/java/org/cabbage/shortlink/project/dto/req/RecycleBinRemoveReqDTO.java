package org.cabbage.shortlink.project.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站彻底删除请求
 */
@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
