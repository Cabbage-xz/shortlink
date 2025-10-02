package org.cabbage.shortlink.admin.remote.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xzcabbage
 * @since 2025/10/02
 * 短链接更新请求
 */
@Data
public class ShortLinkUpdateReqDTO {


    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 短链接
     */
    private String fullShortUrl;

    /**
     * 原有分组标识
     */
    private String originGid;

    /**
     * 分组标识
     */
    private String gid;


    /**
     * 有效期类型 0:永久有效 1:自定义
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validDate;

    /**
     * 描述
     */
    private String description;

}
