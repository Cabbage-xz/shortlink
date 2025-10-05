package org.cabbage.shortlink.common.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/1
 * 短链接分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
