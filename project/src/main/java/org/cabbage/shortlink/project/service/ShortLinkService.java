package org.cabbage.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.cabbage.shortlink.common.dto.req.ShortLinkBatchCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;

import java.util.List;

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

    /**
     * 批量创建短链接
     * @param req 创建请求实体
     * @return 响应
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO req);


    /**
     * 修改短链接
     * @param req 更新请求
     */
    void updateShortLink(ShortLinkUpdateReqDTO req);

    /**
     * 分页查询短链接
     * @param req 分页请求
     * @return 分页结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO req);

    /**
     * 查询分组短链接数量
     * @param gIds 分组标识集合
     * @return 分组与其下短链接数量
     */
    List<ShortLinkCountQueryRespDTO> listShortLinkCount(List<String> gIds);

    /**
     * 短链接跳转
     * @param shortUri 短链接
     * @param req 请求
     * @param res 响应
     */
    void jumpLink(String shortUri, ServletRequest req, ServletResponse res);

    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param shortLinkStatsRecord 短链接统计实体参数
     */
    void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO shortLinkStatsRecord);
}
