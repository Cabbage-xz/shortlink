package org.cabbage.shortlink.admin.service.interfaces;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.common.convention.result.Result;

/**
 * @author xzcabbage
 * @since 2025/10/3
 */
public interface RecycleBinService {

    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLinks(ShortLinkRecycleBinPageReqDTO req);
}
