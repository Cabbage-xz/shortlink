package org.cabbage.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;

/**
 * @author xzcabbage
 * @since 2025/10/1
 * 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序标签
     */
    private String orderTag;
}
