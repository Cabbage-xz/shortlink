package org.cabbage.shortlink.admin.remote;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.dto.req.RecycleBinRecoverReqDTO;
import org.cabbage.shortlink.common.dto.req.RecycleBinRemoveReqDTO;
import org.cabbage.shortlink.common.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkStatsRespDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xzcabbage
 * @since 2025/10/1
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {


    /**
     * 创建短链接
     * @param req 创建短链接请求
     * @return 创建结果
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO req) {
        String resultBody = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(req));
        return JSON.parseObject(resultBody, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     * @param req 修改请求
     */
    default void updateShortLink(ShortLinkUpdateReqDTO req) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(req));

    }


    /**
     * 分页查询短链接
     * @param req 分页查询请求
     * @return 分页查询结果
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkPageReqDTO req) {
        Map<String, Object> params = new HashMap<>();
        params.put("gid", req.getGid());
        params.put("orderTag", req.getOrderTag());
        params.put("current", req.getCurrent());
        params.put("size", req.getSize());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", params);

        return JSON.parseObject(result, new TypeReference<>() {

        });
    }

    /**
     * 查询分组下短链接数量
     * @param gIds 分组ids
     * @return 分组标识与其下短链接数量
     */
    default Result<List<ShortLinkCountQueryRespDTO>> listShortLinkCount(List<String> gIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("gIds", gIds);
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", params);
        return JSON.parseObject(result, new TypeReference<>() {

        });
    }

    /**
     * 依据url获取网站标题
     * @param url url
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(@RequestParam("url") String url) {
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(result, new TypeReference<>() {

        });
    }

    /**
     * 将短链接移动到回收站
     * @param req 请求
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO req) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(req));
    }

    /**
     * 分页查询回收站短链接
     * @param req 分页查询请求
     * @return 分页查询结果
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLinks(ShortLinkRecycleBinPageReqDTO req) {
        Map<String, Object> params = new HashMap<>();
        params.put("gidList", req.getGidList());
        params.put("current", req.getCurrent());
        params.put("size", req.getSize());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", params);

        return JSON.parseObject(result, new TypeReference<>() {

        });
    }

    /**
     * 回收站恢复短链接
     * @param req 恢复短链接请求
     */
    default void recoverShortLink(RecycleBinRecoverReqDTO req) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(req));
    }

    /**
     * 从回收站移除短链接
     * @param req 删除请求
     */
    default void removeShortLink(RecycleBinRemoveReqDTO req) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(req));
    }

    /**
     * 访问单个短链接指定时间内监控数据
     *
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内监控数据
     *
     * @param requestParam 访分组问短链接监控请求参数
     * @return 分组短链接监控信息
     */
    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问短链接监控访问记录请求参数
     * @return 短链接监控访问记录信息
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record", stringObjectMap);
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问分组短链接监控访问记录请求参数
     * @return 分组短链接监控访问记录信息
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group", stringObjectMap);
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }
}
