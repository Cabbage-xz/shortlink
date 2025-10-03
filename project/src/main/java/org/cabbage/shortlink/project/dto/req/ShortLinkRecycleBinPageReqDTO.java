package org.cabbage.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/1
 * 短链接分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
