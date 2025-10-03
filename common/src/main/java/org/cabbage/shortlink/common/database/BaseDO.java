package org.cabbage.shortlink.common.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 数据库基础字段
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BaseDO {
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    @TableLogic(value = "0", delval = "1")
    private Integer delFlag;
}
