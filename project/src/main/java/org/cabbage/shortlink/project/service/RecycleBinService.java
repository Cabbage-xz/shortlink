package org.cabbage.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将制定短链接移至回收站
     * @param req 请求
     */
    void saveRecycleBin(RecycleBinSaveReqDTO req);

    /**
     * 分页查询短链接
     * @param req 分页请求
     * @return 分页结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO req);
}
