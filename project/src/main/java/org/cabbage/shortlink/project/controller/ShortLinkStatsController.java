package org.cabbage.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsRespDTO;
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

    /**
     * 监控分组短链接使用情况
     * @param req 监控请求
     * @return 使用情况
     */
    @RequestMapping(value = "/api/short-link/v1/group", method = RequestMethod.GET)
    public Result<ShortLinkStatsRespDTO> shortLInkStats(ShortLinkGroupStatsReqDTO req) {
        return Results.success(shortLinkStatsService.groupShortLinkStats(req));
    }


    /**
     * 监控单个短链接访问记录
     * @param req 监控访问请求
     * @return 访问情况
     */
    @RequestMapping(value = "/api/short-link/v1/stats/access-record", method = RequestMethod.GET)
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLInkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO req) {
        return Results.success(shortLinkStatsService.shortLInkStatsAccessRecord(req));
    }

    /**
     * 监控单个短链接访问记录
     * @param req 监控访问请求
     * @return 访问情况
     */
    @RequestMapping(value = "/api/short-link/v1/stats/access-record/group", method = RequestMethod.GET)
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLInkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO req) {
        return Results.success(shortLinkStatsService.shortLInkGroupStatsAccessRecord(req));
    }
}
