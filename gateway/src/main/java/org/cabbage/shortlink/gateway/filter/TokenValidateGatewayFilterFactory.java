package org.cabbage.shortlink.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.cabbage.shortlink.gateway.config.Config;
import org.cabbage.shortlink.gateway.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * token验证过滤器
 * @author xzcabbage
 * @since 2025/11/19
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate redisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().toString();
            // 需要校验token的情况
            if (!(!CollectionUtils.isEmpty(config.getWhitePathList()) && config.getWhitePathList().stream().anyMatch(requestPath::startsWith))) {
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                // 已登陆
                if (StringUtils.hasText(username) && StringUtils.hasText(token) &&
                        (userInfo = redisTemplate.opsForHash().get("short-link:lock_user-login:" + username, token)) != null) {
                    JSONObject jsonObject = JSON.parseObject(userInfo.toString());
                    ServerHttpRequest build = exchange.getRequest().mutate()
                            .header("userId", jsonObject.getString("id"))
                            .header("realName", URLEncoder.encode(jsonObject.getString("realName"), StandardCharsets.UTF_8))
                            .build();
                    return chain.filter(exchange.mutate().request(build).build());
                }
                // 未登陆情况
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory factory = response.bufferFactory();
                    GatewayErrorResult result = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("未登录")
                            .build();
                    return factory.wrap(JSON.toJSONString(result).getBytes());
                }));
            }
            return chain.filter(exchange);
        };

    }
}
