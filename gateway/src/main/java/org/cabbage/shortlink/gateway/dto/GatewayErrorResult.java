package org.cabbage.shortlink.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关错误返回
 * @author xzcabbage
 * @since 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayErrorResult {

    /**
     * HTTP状态码
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String message;
}
