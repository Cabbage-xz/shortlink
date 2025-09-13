package org.cabbage.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cabbage.shortlink.admin.common.convention.exception.ClientException;
import org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.cabbage.shortlink.admin.dao.entity.User;
import org.cabbage.shortlink.admin.dao.mapper.UserMapper;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * @author xzcabbage
 * @since 2025/6/9
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public UserRespDTO getUserByUsername(String username) {
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (one == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(one, userRespDTO);
        return userRespDTO;
    }

    /**
     * 依据用户名查询用户是否存在
     * @param username 用户名
     * @return True表示存在 False表示不存在
     */
    @Override
    public Boolean checkUsername(String username) {
        LambdaQueryWrapper<User> eq = Wrappers.lambdaQuery(User.class).eq(User::getUsername, username);
        User user = getOne(eq);
        return user != null;
    }
}
