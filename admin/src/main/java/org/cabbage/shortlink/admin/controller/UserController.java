package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.result.Result;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return user
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO user = userService.getUserByUsername(username);
        if (user == null) {
            return new Result<UserRespDTO>().setCode("-1").setMessage("User not found");
        }
        return new Result<UserRespDTO>().setCode("0").setData(user);
    }
}
