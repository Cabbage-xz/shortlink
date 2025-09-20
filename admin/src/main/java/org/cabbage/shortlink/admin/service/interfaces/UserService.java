package org.cabbage.shortlink.admin.service.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.admin.dao.entity.User;
import org.cabbage.shortlink.admin.dto.req.UserLoginReqDTO;
import org.cabbage.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.cabbage.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回True 不存在返回False
     */
    Boolean checkUsername(String username);

    /**
     * 注册用户
     *
     * @param req 注册用户请求参数
     */
    void register(UserRegisterReqDTO req);

    /**
     * 修改用户
     *
     * @param req 修改用户请求参数
     */
    void updateInfo(UserUpdateReqDTO req);

    /**
     * 用户登录
     *
     * @param req 用户登录请求
     * @return 用户登录返回响应
     */
    UserLoginRespDTO login(UserLoginReqDTO req);

    /**
     * 检查用户是否登陆
     *
     * @param username 用户名
     * @param token token值
     * @return 返回是否登陆结果
     */
    Boolean checkLogin(String username, String token);
}
