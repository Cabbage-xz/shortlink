package org.cabbage.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.common.convention.exception.ServiceException;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dao.mapper.ShortLinkMapper;
import org.cabbage.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.cabbage.shortlink.project.toolkit.HashUtil;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_ALREADY_EXIST;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_CREATE_TIMES_TOO_MANY;

/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCachePenetrationBloomFilter;

    /**
     * 创建短链接
     *
     * @param req 创建请求实体
     * @return 短链接
     */
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req) {
        String shortUri = generateShortUrl(req);
        String fullShortUrl = req.getDomain() + "/" + shortUri;
        ShortLinkDO linkDO = BeanUtil.toBean(req, ShortLinkDO.class);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        try {
            save(linkDO);
        } catch (DuplicateKeyException exception) {
            log.warn("short url {} already exists", fullShortUrl);
            throw new ServiceException(SHORT_LINK_ALREADY_EXIST);
        }
        shortUriCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(req.getGid())
                .originUrl(req.getOriginUrl())
                .fullShortUrl(fullShortUrl)
                .build();
    }

    private String generateShortUrl(ShortLinkCreateReqDTO req) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException(SHORT_LINK_CREATE_TIMES_TOO_MANY);
            }
            String salt = UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(req.getOriginUrl() + salt);
            String fullUrl = req.getDomain() + "/" + shortUri;
            if (!shortUriCachePenetrationBloomFilter.contains(fullUrl)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
