package org.cabbage.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站恢复请求
 */
@Data
public class RecycleBinRecoverReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
