package org.cabbage.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dao.mapper.ShortLinkMapper;
import org.cabbage.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.cabbage.shortlink.project.toolkit.HashUtil;
import org.springframework.stereotype.Service;

/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接接口实现层
 */
@Slf4j
@Service
public class ShortLinkImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    /**
     * 创建短链接
     *
     * @param req 创建请求实体
     * @return 短链接
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req) {
        String shortUri = generateShortUrl(req.getOriginUrl());
        String fullShortUrl = req.getDomain() + "/" + shortUri;
        ShortLinkDO linkDO = BeanUtil.toBean(req, ShortLinkDO.class);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        save(linkDO);
        return ShortLinkCreateRespDTO.builder()
                .gid(req.getGid())
                .originUrl(req.getOriginUrl())
                .fullShortUrl(fullShortUrl)
                .build();
    }

    private String generateShortUrl(String originUrl) {
        return HashUtil.hashToBase62(originUrl);
    }
}
