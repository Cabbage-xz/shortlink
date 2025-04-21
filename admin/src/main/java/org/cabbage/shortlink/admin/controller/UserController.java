package org.cabbage.shortlink.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return user
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public String getUserByUsername(@PathVariable("username") String username) {
        return "Hi " + username;
    }
}
