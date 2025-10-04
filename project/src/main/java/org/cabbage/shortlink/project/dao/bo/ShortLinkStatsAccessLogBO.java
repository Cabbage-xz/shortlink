package org.cabbage.shortlink.project.dao.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xzcabbage
 * @since 2025/10/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsAccessLogBO {

    /**
     * IP
     */
    private String ip;

    /**
     * 计数
     */
    private Integer cnt;

    /**
     * 老访客数量
     */
    private Integer oldUserCount;

    /**
     * 新访客数量
     */
    private Integer newUserCount;
}
