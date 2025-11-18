package org.cabbage.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/10/5
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService remoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return remoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return remoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return remoteService.shortLinkStatsAccessRecord(requestParam);
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return remoteService.groupShortLinkStatsAccessRecord(requestParam);
    }
}
