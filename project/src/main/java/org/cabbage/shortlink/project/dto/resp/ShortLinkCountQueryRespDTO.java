package org.cabbage.shortlink.project.dto.resp;

import lombok.Data;

/**
 * @author xzcabbage
 * @since 2025/10/2
 * 短链接分组查询返回参数
 */
@Data
public class ShortLinkCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer shortLinkCount;
}
