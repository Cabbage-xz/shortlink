package org.cabbage.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cabbage.shortlink.common.database.BaseDO;

import java.time.LocalDate;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_device_stats")
public class LinkDeviceStatsDO extends BaseDO {

    /**
     * ID
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问设备
     */
    private String device;
}