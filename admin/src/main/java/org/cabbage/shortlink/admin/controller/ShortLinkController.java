package org.cabbage.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.admin.remote.ShortLinkRemoteService;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkBaseInfoRespDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import org.cabbage.shortlink.admin.toolkit.EasyExcelWebUtil;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.common.dto.req.ShortLinkBatchCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/1
 * 短链接后管控制层
 */
@RestController
@Slf4j
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {

    };

    /**
     * 创建短链接
     * @param req 创建请求实体
     * @return 响应
     */
    @RequestMapping(value = "/api/short-link/admin/v1/create", method = RequestMethod.POST)
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO req) {
        return shortLinkRemoteService.createShortLink(req);
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @RequestMapping(value = "/api/short-link/admin/v1/create/batch", method = RequestMethod.POST)
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    /**
     * 修改短链接信息
     * @return void
     */
    @RequestMapping(value = "/api/short-link/admin/v1/update", method = RequestMethod.POST)
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO req) {
        shortLinkRemoteService.updateShortLink(req);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/page", method = RequestMethod.GET)
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkPageReqDTO req) {
        return shortLinkRemoteService.pageShortLinks(req);
    }

    /**
     * 查询分组下短链接数量
     * @param gIds 分组标识
     * @return 分组与其下短链接数
     */
    @RequestMapping(value = "/api/short-link/admin/v1/count", method = RequestMethod.GET)
    public Result<List<ShortLinkCountQueryRespDTO>> listShortLinkCount(@RequestParam("gIds") List<String> gIds) {
        return shortLinkRemoteService.listShortLinkCount(gIds);
    }

}
