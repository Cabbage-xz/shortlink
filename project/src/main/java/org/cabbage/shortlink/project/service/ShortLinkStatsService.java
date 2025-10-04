package org.cabbage.shortlink.project.service;

import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
public interface ShortLinkStatsService {

    /**
     * 监控单个短链接使用情况
     * @param req 监控请求
     * @return 使用情况
     */
    ShortLinkStatsRespDTO singleShortLinkStats(ShortLinkStatsReqDTO req);
}
