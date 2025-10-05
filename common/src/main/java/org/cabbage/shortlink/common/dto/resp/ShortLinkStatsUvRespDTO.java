package org.cabbage.shortlink.common.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xzcabbage
 * @since 2025/10/4
 * 访客类型响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsUvRespDTO {
    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 访客类型
     */
    private String uvType;

    /**
     * 占比
     */
    private Double ratio;
}
