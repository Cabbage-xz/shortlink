package org.cabbage.shortlink.gateway.config;

import lombok.Data;

import java.util.List;

/**
 * 过滤器配置
 * @author xzcabbage
 * @since 2025/11/19
 */
@Data
public class Config {
    private List<String> whitePathList;
}
