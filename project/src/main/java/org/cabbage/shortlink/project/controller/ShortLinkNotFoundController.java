package org.cabbage.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 链接不存在时重定向
 */
@Controller
public class ShortLinkNotFoundController {
    /**
     * 短链接不存在跳转页面
     */
    @RequestMapping("/page/notfound")
    public String notfound() {
        return "notfound";
    }
}
