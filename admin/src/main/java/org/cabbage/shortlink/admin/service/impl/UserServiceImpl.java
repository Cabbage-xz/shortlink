package org.cabbage.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.exception.ClientException;
import org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.cabbage.shortlink.admin.dao.entity.User;
import org.cabbage.shortlink.admin.dao.mapper.UserMapper;
import org.cabbage.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.UserService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static org.cabbage.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum.USER_NAME_EXIST;
import static org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;

/**
 * 
 * 
 * @author xzcabbage
 * @since 2025/6/9
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

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
//        LambdaQueryWrapper<User> eq = Wrappers.lambdaQuery(User.class).eq(User::getUsername, username);
//        User user = getOne(eq);
//        return user != null;
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 注册用户
     * @param req 注册用户请求参数
     */
    @Override
    public void register(UserRegisterReqDTO req) {
        if (checkUsername(req.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + req.getUsername());
        try {
            if (lock.tryLock()) {
                if (!save(BeanUtil.toBean(req, User.class))) {
                    throw new ClientException(USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(req.getUsername());
                return;
            }
            throw new ClientException(USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }
}
