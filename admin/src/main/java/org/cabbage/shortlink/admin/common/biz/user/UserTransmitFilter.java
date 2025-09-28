package org.cabbage.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_USER_LOGIN_KEY;


/**
 * @author xzcabbage
 * @since 2025/9/21
 * 用户信息传输拦截器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {
    private final StringRedisTemplate stringRedisTemplate;

    private final List<String> IGNORE_URLS = Arrays.asList(
            "/api/short-link/admin/v1/user/login",
            "/api/short-link/admin/v1/user/has-username",
            "/api/short-link/admin/v1/user/register"
    );


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        if (!IGNORE_URLS.contains(requestURI)) {
            String username = httpServletRequest.getHeader("username");
            String token = httpServletRequest.getHeader("token");
            if (!StrUtil.isAllNotBlank(username, token)) {
                // todo 抛出token异常
            }
            Object userInfoJsonStr;
            try {
                userInfoJsonStr = stringRedisTemplate.opsForHash().get(LOCK_USER_LOGIN_KEY + username, token);
                if (userInfoJsonStr == null) {
                    // todo 抛出token异常
                    return;
                }
            } catch (Exception e) {
                // todo 抛出token异常
                return;
            }
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}
