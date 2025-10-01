package org.cabbage.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.common.convention.result.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xzcabbage
 * @since 2025/10/1
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {


    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO req) {
        String resultBody = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(req));
        return JSON.parseObject(resultBody, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLinks(ShortLinkPageReqDTO req) {
        Map<String, Object> params = new HashMap<>();
        params.put("gid", req.getGid());
        params.put("current", req.getCurrent());
        params.put("size", req.getSize());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", params);

        return JSON.parseObject(result, new TypeReference<>() {

        });
    }

}
