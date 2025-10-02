package org.cabbage.shortlink.admin.controller;

import org.cabbage.shortlink.admin.remote.ShortLinkRemoteService;
import org.cabbage.shortlink.common.convention.result.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * url标题控制层
 */
@RestController
public class UrlTitleController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {

    };

    /**
     * 依据url获取网站标题
     * @param url url
     * @return 网站标题
     */
    @RequestMapping(value = "/api/short-link/admin/v1/title", method = RequestMethod.GET)
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return shortLinkRemoteService.getTitleByUrl(url);
    }
}
