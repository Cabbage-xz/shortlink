package org.cabbage.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;


    /**
     * 创建短链接
     * @param req 创建请求实体
     * @return 响应
     */
    @RequestMapping(value = "/api/short-link/v1/create", method = RequestMethod.POST)
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO req) {
        return Results.success(shortLinkService.createShortLink(req));
    }


    /**
     * 分页查询短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/v1/page", method = RequestMethod.GET)
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(@RequestBody ShortLinkPageReqDTO req) {
        return Results.success(shortLinkService.pageShortLink(req));
    }
}
