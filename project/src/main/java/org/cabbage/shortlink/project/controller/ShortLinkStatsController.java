package org.cabbage.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import org.cabbage.shortlink.project.service.ShortLinkStatsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/10/4
 * 短链接监控
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;


    /**
     * 监控单个短链接使用情况
     * @param req 监控请求
     * @return 使用情况
     */
    @RequestMapping(value = "/api/short-link/v1/stats", method = RequestMethod.GET)
    public Result<ShortLinkStatsRespDTO> shortLInkStats(ShortLinkStatsReqDTO req) {
        return Results.success(shortLinkStatsService.singleShortLinkStats(req));
    }
}
