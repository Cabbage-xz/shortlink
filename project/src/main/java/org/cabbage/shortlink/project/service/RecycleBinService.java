package org.cabbage.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dto.req.RecycleBinSaveReqDTO;

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
}
