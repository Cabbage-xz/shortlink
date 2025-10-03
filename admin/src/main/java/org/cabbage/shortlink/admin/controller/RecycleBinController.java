package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.remote.ShortLinkRemoteService;
import org.cabbage.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
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
}
