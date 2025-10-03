package org.cabbage.shortlink.project.toolkit;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * 
 * @author xzcabbage
 * @since 2025/10/4
 * 获取用户真实IP
 */
public class IPUtil {
    private static final String UNKNOWN = "unknown";
    private static final String SEPARATOR = ",";

    /**
     * 获取客户端真实IP地址
     */
    public static String getRealIp(ServletRequest request) {
        if (!(request instanceof HttpServletRequest)) {
            return request.getRemoteAddr();
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ip = getIpFromHeaders(httpRequest);

        // 处理 IPv6 本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = getLocalIp();
        }

        return ip;
    }

    /**
     * 从请求头中获取IP
     */
    private static String getIpFromHeaders(HttpServletRequest request) {
        String ip;

        // 优先级从高到低检查各个请求头
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR",
                "X-Cluster-Client-IP"
        };

        for (String header : headers) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For 可能包含多个IP，格式: client, proxy1, proxy2
                // 取第一个（最左边的）才是真实客户端IP
                if (ip.contains(SEPARATOR)) {
                    ip = ip.split(SEPARATOR)[0].trim();
                }
                return ip;
            }
        }

        // 所有请求头都没有，使用 RemoteAddr
        return request.getRemoteAddr();
    }

    /**
     * 验证IP是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null
                && !ip.isEmpty()
                && !UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 获取本机IP
     */
    @SneakyThrows
    private static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}
