package org.cabbage.shortlink.admin.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;

/**
 * @author xzcabbage
 * @since 2025/10/3
 */
public interface RecycleBinService {

    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLinks(ShortLinkRecycleBinPageReqDTO req);
}
