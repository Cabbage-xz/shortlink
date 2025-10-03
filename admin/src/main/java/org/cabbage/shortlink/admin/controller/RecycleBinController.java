package org.cabbage.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.remote.ShortLinkRemoteService;
import org.cabbage.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.RecycleBinService;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
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

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {

    };

    /**
     * 将短链接保存到回收站
     * @param req 请求
     * @return void
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/save", method = RequestMethod.POST)
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO req) {
        shortLinkRemoteService.saveRecycleBin(req);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/page", method = RequestMethod.GET)
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkRecycleBinPageReqDTO req) {
        return recycleBinService.pageRecycleBinShortLinks(req);
    }

    /**
     * 回收站恢复短链接
     * @param req 恢复请求
     * @return 恢复结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/recycle-bin/recover", method = RequestMethod.POST)
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO req) {
        shortLinkRemoteService.recoverShortLink(req);
        return Results.success();
    }
}
