package org.cabbage.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cabbage.shortlink.common.database.BaseDO;

import java.time.LocalDateTime;

/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link")
public class ShortLinkDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 域名
     */
    private String domain;

    /**
     * 短链接
     */
    private String shortUri;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 点击量
     */
    private Integer clickNum;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 图标
     */
    private String favicon;

    /**
     * 启用标识 0:启用 1:未启用
     */
    private Integer enableStatus;

    /**
     * 常见类型 0:接口创建 1:控制台创建
     */
    private Integer createType;

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
