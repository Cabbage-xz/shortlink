package org.cabbage.shortlink.common.dto.req;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站保存请求
 */
@Data
public class RecycleBinSaveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
