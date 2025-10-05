package org.cabbage.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cabbage.shortlink.common.database.BaseDO;

/**
 * @author xzcabbage
 * @since 2025/10/2
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_goto")
public class LinkGotoDO extends BaseDO {

    /**
     * id
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
}
