package org.cabbage.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.dto.req.RecycleBinRecoverReqDTO;
import org.cabbage.shortlink.common.dto.req.RecycleBinRemoveReqDTO;
import org.cabbage.shortlink.common.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkBatchCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台远程调用服务
 *
 * @author xzcabbage
 * @since 2025/11/18
 */
@FeignClient("short-link-project")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接
     *
     * @param req 创建短链接请求
     * @return 创建结果
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO req);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 短链接批量创建响应
     */
    @PostMapping("/api/short-link/v1/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 修改短链接
     *
     * @param req 修改请求
     */
    @PostMapping("/api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO req);

    /**
     * 分页查询短链接
     *
     * @param gid      分组
     * @param orderTag 排序
     * @param current  当前页
     * @param size     总大小
     * @return 分页查询结果
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLinks(@RequestParam("gid") String gid,
                                                      @RequestParam("orderTag") String orderTag,
                                                      @RequestParam("current") Long current,
                                                      @RequestParam("size") Long size);


    /**
     * 查询分组下短链接数量
     *
     * @param gIds 分组ids
     * @return 分组标识与其下短链接数量
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkCountQueryRespDTO>> listShortLinkCount(@RequestParam("gIds") List<String> gIds);

    /**
     * 依据url获取网站标题
     *
     * @param url url
     * @return 网站标题
     */
    @GetMapping("/api/short-link/v1/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);

    /**
     * 将短链接移动到回收站
     *
     * @param req 请求
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO req);

    /**
     * 分页查询回收站短链接
     *
     * @param gIdList 分组集合
     * @param current 当前页
     * @param size    数据量
     * @return 分页查询结果
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLinks(@RequestParam("gidList") List<String> gIdList,
                                                                @RequestParam("current") Long current,
                                                                @RequestParam("size") Long size);

    /**
     * 回收站恢复短链接
     *
     * @param req 恢复短链接请求
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    void recoverShortLink(@RequestBody RecycleBinRecoverReqDTO req);

    /**
     * 从回收站移除短链接
     *
     * @param req 删除请求
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    void removeShortLink(@RequestBody RecycleBinRemoveReqDTO req);

    /**
     * 访问单个短链接指定时间内监控数据
     *
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    @GetMapping("/api/short-link/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@SpringQueryMap ShortLinkStatsReqDTO requestParam);


    /**
     * 访问分组短链接指定时间内监控数据
     *
     * @param requestParam 访分组问短链接监控请求参数
     * @return 分组短链接监控信息
     */
    @GetMapping("/api/short-link/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@SpringQueryMap ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问短链接监控访问记录请求参数
     * @return 短链接监控访问记录信息
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@SpringQueryMap ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问分组短链接监控访问记录请求参数
     * @return 分组短链接监控访问记录信息
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@SpringQueryMap ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}
