package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.result.Result;
import org.cabbage.shortlink.admin.common.convention.result.Results;
import org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
            return new Result<UserRespDTO>().setCode(UserErrorCodeEnum.USER_NULL.code()).setMessage(UserErrorCodeEnum.USER_NULL.message());
        }
        return Results.success(user);
    }

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return True表示存在 False表示不存在
     */
    @GetMapping("/api/shortlink/v1/user/has-username")
    public Result<Boolean> checkHasUsername(@RequestParam("username") String username) {
        return Results.success(userService.checkUsername(username));
    }
}
