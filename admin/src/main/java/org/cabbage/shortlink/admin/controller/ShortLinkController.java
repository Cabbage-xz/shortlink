package org.cabbage.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.admin.remote.dto.ShortLinkRemoteService;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.common.convention.result.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
     * 分页查询短链接
     * @param req 请求
     * @return 分页结果
     */
    @RequestMapping(value = "/api/short-link/admin/v1/page", method = RequestMethod.GET)
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkPageReqDTO req) {
        return shortLinkRemoteService.pageShortLinks(req);
    }

}
