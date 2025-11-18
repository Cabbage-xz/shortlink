package org.cabbage.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.cabbage.shortlink.admin.service.interfaces.RecycleBinService;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.common.dto.req.RecycleBinRecoverReqDTO;
import org.cabbage.shortlink.common.dto.req.RecycleBinRemoveReqDTO;
import org.cabbage.shortlink.common.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;
    private final ShortLinkActualRemoteService remoteService;

    /**
     * 将短链接保存到回收站
     * @param req 请求
     * @return void
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/save", method = RequestMethod.POST)
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO req) {
        remoteService.saveRecycleBin(req);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/page", method = RequestMethod.GET)
    public Result<Page<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkRecycleBinPageReqDTO req) {
        return recycleBinService.pageRecycleBinShortLinks(req);
    }

    /**
     * 回收站恢复短链接
     * @param req 恢复请求
     * @return 恢复结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/recover", method = RequestMethod.POST)
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO req) {
        remoteService.recoverShortLink(req);
        return Results.success();
    }

    /**
     * 回收站彻底删除短链接
     * @param req 恢复请求
     * @return 恢复结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/remove", method = RequestMethod.POST)
    public Result<Void> removeShortLink(@RequestBody RecycleBinRemoveReqDTO req) {
        remoteService.removeShortLink(req);
        return Results.success();
    }
}
