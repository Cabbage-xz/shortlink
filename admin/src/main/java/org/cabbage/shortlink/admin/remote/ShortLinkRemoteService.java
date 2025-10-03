package org.cabbage.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.cabbage.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.common.convention.result.Result;
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
}
