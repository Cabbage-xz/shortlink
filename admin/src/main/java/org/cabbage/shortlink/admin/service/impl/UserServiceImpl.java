package org.cabbage.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.biz.user.UserContext;
import org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.cabbage.shortlink.admin.dao.entity.User;
import org.cabbage.shortlink.admin.dao.mapper.UserMapper;
import org.cabbage.shortlink.admin.dto.req.UserLoginReqDTO;
import org.cabbage.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.cabbage.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.cabbage.shortlink.admin.service.interfaces.UserService;
import org.cabbage.shortlink.common.convention.exception.ClientException;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum.USER_NAME_EXIST;
import static org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum.USER_PASSWORD_ERROR;
import static org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;
import static org.cabbage.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_ERROR;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_USER_LOGIN_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;

/**
 * @author xzcabbage
 * @since 2025/6/9
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final GroupService groupService;

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

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
     *
     * @param username 用户名
     * @return True表示存在 False表示不存在
     */
    @Override
    public Boolean checkUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 注册用户
     *
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
                groupService.saveGroup(req.getUsername(), "default");
                return;
            }
            throw new ClientException(USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 修改用户
     *
     * @param req 修改用户请求参数
     */
    @Override
    public void updateInfo(UserUpdateReqDTO req) {
        if (!Objects.equals(req.getUsername(), UserContext.getUsername())) {
            throw new ClientException("当前登录用户修改请求异常");
        }
        update(BeanUtil.toBean(req, User.class),
                new LambdaUpdateWrapper<>(User.class).eq(User::getUsername, req.getUsername()));
    }

    /**
     * 用户登录
     *
     * @param req 用户登录请求
     * @return 用户登录响应实体
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO req) {
        User one = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (one == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        } else if (!one.getPassword().equals(req.getPassword())) {
            throw new ClientException(USER_PASSWORD_ERROR);
        }

        String loginKey = LOCK_USER_LOGIN_KEY + req.getUsername();
        Map<Object, Object> loginInfo = stringRedisTemplate.opsForHash().entries(loginKey);

        if (!loginInfo.isEmpty()) {
            // 已登录，返回现有 uuid
            String existingUuid = (String) loginInfo.keySet().iterator().next();
            // 刷新过期时间
            stringRedisTemplate.expire(loginKey, 30L, TimeUnit.MINUTES);
            return new UserLoginRespDTO(existingUuid);
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(loginKey,
                uuid, JSON.toJSONString(one));
        stringRedisTemplate.expire(loginKey, 30L, TimeUnit.MINUTES);

        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检查是否登陆
     *
     * @param username 用户名
     * @param token    token值
     * @return 是否登陆
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().hasKey(LOCK_USER_LOGIN_KEY + username, token);
    }

    /**
     * 用户退出登录
     *
     * @param username 用户名
     * @param token    token
     */
    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(LOCK_USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException(USER_TOKEN_ERROR);
    }
}
