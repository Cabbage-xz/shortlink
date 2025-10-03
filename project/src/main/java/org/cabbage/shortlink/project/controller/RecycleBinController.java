package org.cabbage.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import org.cabbage.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.project.service.RecycleBinService;
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


    @RequestMapping(value = "/api/short-link/v1/recycle-bin/save", method = RequestMethod.POST)
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO req) {
        recycleBinService.saveRecycleBin(req);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/v1/recycle-bin/page", method = RequestMethod.GET)
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkRecycleBinPageReqDTO req) {
        return Results.success(recycleBinService.pageShortLink(req));
    }

    /**
     * 回收站恢复短链接
     * @param req 恢复请求
     * @return 恢复结果
     */
    @RequestMapping(value = "/api/short-link/v1/recycle-bin/recover", method = RequestMethod.POST)
    public Result<Void> recoverShortLink(@RequestBody RecycleBinRecoverReqDTO req) {
        recycleBinService.recoverShortLink(req);
        return Results.success();
    }
}
