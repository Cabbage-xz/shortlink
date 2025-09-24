package org.cabbage.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     *
     * @param req 创建请求实体
     * @return 短链接响应
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req);
}
