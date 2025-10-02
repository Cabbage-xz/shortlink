package org.cabbage.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 修改短链接信息
     * @return void
     */
    @RequestMapping(value = "/api/short-link/v1/update", method = RequestMethod.POST)
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO req) {
        shortLinkService.updateShortLink(req);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/v1/page", method = RequestMethod.GET)
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkPageReqDTO req) {
        return Results.success(shortLinkService.pageShortLink(req));
    }

    /**
     * 查询分组短链接数量
     * @param gIds 分组标识集合
     * @return 分组与其下短链接数量
     */
    @RequestMapping(value = "/api/short-link/v1/count", method = RequestMethod.GET)
    public Result<List<ShortLinkCountQueryRespDTO>> listShortLinkCount(@RequestParam("gIds") List<String> gIds) {
        return Results.success(shortLinkService.listShortLinkCount(gIds));
    }

    /**
     * 短链接跳转
     * @param shortUrl 完整短链接
     * @param req 请求
     * @param res 响应
     */
    @RequestMapping(value = "{short-url}", method = RequestMethod.GET)
    public void jumpLink(@PathVariable("short-url") String shortUrl, ServletRequest req, ServletResponse res) {
        shortLinkService.jumpLink(shortUrl, req, res);
    }
}
