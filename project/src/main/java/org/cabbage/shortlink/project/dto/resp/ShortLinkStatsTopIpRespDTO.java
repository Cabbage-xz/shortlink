package org.cabbage.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xzcabbage
 * @since 2025/10/4
 * 高频ip响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsTopIpRespDTO {
    /**
     * 统计
     */
    private Integer cnt;

    /**
     * IP
     */
    private String ip;
}
