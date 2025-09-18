package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.result.Result;
import org.cabbage.shortlink.admin.common.convention.result.Results;
import org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.cabbage.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.cabbage.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.UserService;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/api/short-link/v1/user/{username}")
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
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> checkHasUsername(@RequestParam("username") String username) {
        return Results.success(userService.checkUsername(username));
    }

    /**
     * 注册用户
     * @param req 用户请求实体
     * @return 注册结果
     */
    @RequestMapping(value = "/api/short-link/v1/user/register", method = RequestMethod.POST)
    public Result<Void> register(@RequestBody UserRegisterReqDTO req) {
        userService.register(req);
        return Results.success();
    }

    /**
     * 修改用户信息
     * @param req 用户请求实体
     * @return 修改结果
     */
    @RequestMapping(value = "/api/short-link/v1/user/update", method = RequestMethod.POST)
    public Result<Void> updateInfo(@RequestBody UserUpdateReqDTO req) {
        userService.updateInfo(req);
        return Results.success();
    }
}
