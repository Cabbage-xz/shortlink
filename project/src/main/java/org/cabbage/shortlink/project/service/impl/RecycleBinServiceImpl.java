package org.cabbage.shortlink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dao.mapper.ShortLinkMapper;
import org.cabbage.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.project.service.RecycleBinService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 将制定短链接移至回收站
     * @param req 请求
     */
    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO req) {
        update(new LambdaUpdateWrapper<ShortLinkDO>()
                .eq(ShortLinkDO::getGid, req.getGid())
                .eq(ShortLinkDO::getFullShortUrl, req.getFullShortUrl())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .set(ShortLinkDO::getEnableStatus, 1));

        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, req.getFullShortUrl()));

    }
}
