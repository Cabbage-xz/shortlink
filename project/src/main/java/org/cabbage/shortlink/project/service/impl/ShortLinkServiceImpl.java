package org.cabbage.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.common.convention.exception.ClientException;
import org.cabbage.shortlink.common.convention.exception.ServiceException;
import org.cabbage.shortlink.common.dto.req.ShortLinkBatchCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.project.common.enums.ValidDateTypeEnum;
import org.cabbage.shortlink.project.config.GoToDomainWhiteListConfiguration;
import org.cabbage.shortlink.project.dao.entity.LinkAccessLogsDO;
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkBrowserStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkDeviceStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkGotoDO;
import org.cabbage.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkOsStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkBrowserStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkDeviceStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkNetworkStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkOsStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import org.cabbage.shortlink.project.dao.mapper.ShortLinkMapper;
import org.cabbage.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkBaseInfoRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import org.cabbage.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import org.cabbage.shortlink.project.service.LinkGotoService;
import org.cabbage.shortlink.project.service.LinkStatsTodayService;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.cabbage.shortlink.project.toolkit.HashUtil;
import org.cabbage.shortlink.project.toolkit.LinkUtil;
import org.cabbage.shortlink.project.toolkit.ReqUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_GID_UPDATE_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_GOTO_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.SHORT_LINK_STATS_UIP_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.SHORT_LINK_STATS_UV_KEY;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_ALREADY_EXIST;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_ANALYSE_ERROR;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_CREATE_TIMES_TOO_MANY;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_NOT_EXIST;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_PROTECT_ERROR;


/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCachePenetrationBloomFilter;

    private final LinkGotoService linkGotoService;
    private final LinkStatsTodayService linkStatsTodayService;
    private final ShortLinkMapper shortLinkMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    private final GoToDomainWhiteListConfiguration goToDomainWhiteListConfiguration;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAMapKey;

    @Value("${short-link.domain.default}")
    private String defaultDomain;

    /**
     * 创建短链接
     *
     * @param req 创建请求实体
     * @return 短链接
     */
    @Transactional
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req) {
        verificationWhiteLinkList(req.getOriginUrl());

        String shortUri = generateShortUrl(req);
        String fullShortUrl = defaultDomain + "/" + shortUri;
        ShortLinkDO linkDO = BeanUtil.toBean(req, ShortLinkDO.class);
        linkDO.setDomain(defaultDomain);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setFavicon(getFavicon(req.getOriginUrl()));
        linkDO.setTotalPv(0);
        linkDO.setTotalUv(0);
        linkDO.setTotalUip(0);
        linkDO.setDelTime(0L);
        try {
            save(linkDO);
            linkGotoService.save(LinkGotoDO.builder().fullShortUrl(fullShortUrl).gid(req.getGid()).build());
        } catch (DuplicateKeyException exception) {
            log.warn("short url {} already exists", fullShortUrl);
            throw new ServiceException(SHORT_LINK_ALREADY_EXIST);
        }
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                req.getOriginUrl(), LinkUtil.getLinkCacheValidTime(req.getValidDate()), TimeUnit.MILLISECONDS);
        shortUriCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(req.getGid())
                .originUrl(req.getOriginUrl())
                .fullShortUrl("http://" + fullShortUrl)
                .build();
    }

    /**
     * 批量创建短链接
     * @param req 创建请求实体
     * @return 响应
     */
    @Transactional
    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO req) {
        List<String> originUrls = req.getOriginUrls();
        List<String> describes = req.getDescriptions();

        // 1. 准备批量数据
        List<ShortLinkDO> linkDOList = new ArrayList<>();
        List<LinkGotoDO> gotoList = new ArrayList<>();
        List<ShortLinkBaseInfoRespDTO> result = new ArrayList<>();
        Map<String, String> redisCache = new HashMap<>();
        List<String> bloomFilterKeys = new ArrayList<>();

        for (int i = 0; i < originUrls.size(); i++) {
            try {
                String originUrl = originUrls.get(i);
                String description = describes.get(i);

                // 生成短链接
                ShortLinkCreateReqDTO tempReq = BeanUtil.toBean(req, ShortLinkCreateReqDTO.class);
                verificationWhiteLinkList(originUrl);
                tempReq.setOriginUrl(originUrl);
                tempReq.setDescription(description);

                String shortUri = generateShortUrl(tempReq);
                String fullShortUrl = defaultDomain + "/" + shortUri;

                // 构建 ShortLinkDO
                ShortLinkDO linkDO = BeanUtil.toBean(tempReq, ShortLinkDO.class);
                linkDO.setDomain(defaultDomain);
                linkDO.setShortUri(shortUri);
                linkDO.setFullShortUrl(fullShortUrl);
                linkDO.setFavicon(getFavicon(originUrl));
                linkDO.setTotalPv(0);
                linkDO.setTotalUv(0);
                linkDO.setTotalUip(0);
                linkDOList.add(linkDO);

                // 构建 LinkGotoDO
                LinkGotoDO gotoDO = LinkGotoDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(req.getGid())
                        .build();
                gotoList.add(gotoDO);

                // 准备 Redis 缓存数据
                String redisKey = String.format(GOTO_SHORT_LINK_KEY, fullShortUrl);
                redisCache.put(redisKey, originUrl);

                // 准备布隆过滤器数据
                bloomFilterKeys.add(fullShortUrl);

                // 构建响应
                ShortLinkBaseInfoRespDTO linkBaseInfoRespDTO = ShortLinkBaseInfoRespDTO.builder()
                        .fullShortUrl("http://" + fullShortUrl)
                        .originUrl(originUrl)
                        .describe(description)
                        .build();
                result.add(linkBaseInfoRespDTO);

            } catch (Exception ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrls.get(i), ex);
            }
        }

        // 2. 批量插入数据库
        if (!linkDOList.isEmpty()) {
            try {
                // MyBatis-Plus 批量插入（单次SQL）
                this.saveBatch(linkDOList);
                linkGotoService.saveBatch(gotoList);

                // 3. 批量写入 Redis（使用 Pipeline）
                batchSetRedisCache(redisCache, req.getValidDate());

                // 4. 批量添加到布隆过滤器
                bloomFilterKeys.forEach(shortUriCachePenetrationBloomFilter::add);

            } catch (DuplicateKeyException exception) {
                log.error("批量创建短链接出现重复", exception);
                throw new ServiceException("存在重复的短链接");
            }
        }

        return ShortLinkBatchCreateRespDTO.builder()
                .total(result.size())
                .baseLinkInfos(result)
                .build();
    }

    /**
     * 更新短链接
     * @param req 更新请求
     */
    @Transactional
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO req) {
        verificationWhiteLinkList(req.getOriginUrl());
        // 先依据full url查询
        ShortLinkDO one = getOne(new LambdaQueryWrapper<ShortLinkDO>()
                .eq(ShortLinkDO::getFullShortUrl, req.getFullShortUrl())
                .eq(ShortLinkDO::getGid, req.getOriginGid())
                .eq(ShortLinkDO::getEnableStatus, 0));
        if (one == null) {
            throw new ServiceException(SHORT_LINK_NOT_EXIST);
        }
        ShortLinkDO updateDO = BeanUtil.toBean(req, ShortLinkDO.class);
        if (Objects.equals(req.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType())) {
            updateDO.setValidDate(LocalDateTime.of(2099, 12, 31, 23, 59, 59));
        }
        // 若修改原始链接，则需要更新缓存
        if (!one.getOriginUrl().equals(req.getOriginUrl())) {
            stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, req.getFullShortUrl()),
                    req.getOriginUrl(), LinkUtil.getLinkCacheValidTime(req.getValidDate()), TimeUnit.MILLISECONDS);
        }
        if (req.getOriginGid().equals(req.getGid())) {
            // 分组相同 直接更新
            update(updateDO, new LambdaUpdateWrapper<ShortLinkDO>()
                    .eq(ShortLinkDO::getGid, req.getOriginGid())
                    .eq(ShortLinkDO::getFullShortUrl, req.getFullShortUrl())
                    .eq(ShortLinkDO::getEnableStatus, 0));
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, req.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            // 阻塞等所有读锁释放
            rLock.lock();

            try {
                // 分组不同 先删后插
                update(new LambdaUpdateWrapper<ShortLinkDO>()
                        .eq(ShortLinkDO::getGid, req.getOriginGid())
                        .eq(ShortLinkDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(ShortLinkDO::getEnableStatus, 0)
                        .set(ShortLinkDO::getDelTime, System.currentTimeMillis())
                        .set(ShortLinkDO::getDelFlag, 1));
                // 把原先属性复制到更新对象中
                ShortLinkDO insertDO = ShortLinkDO.builder()
                        .domain(one.getDomain())
                        .shortUri(one.getShortUri())
                        .fullShortUrl(one.getFullShortUrl())
                        .clickNum(one.getClickNum())
                        .favicon(one.getFavicon())
                        .enableStatus(one.getEnableStatus())
                        .createType(one.getCreateType())
                        .originUrl(updateDO.getOriginUrl())
                        .gid(updateDO.getGid())
                        .validDateType(updateDO.getValidDateType())
                        .validDate(updateDO.getValidDate())
                        .description(updateDO.getDescription())
                        .build();
                save(insertDO);


                List<LinkStatsTodayDO> linkStatsTodayDOList = linkStatsTodayMapper.selectList(new LambdaQueryWrapper<>(LinkStatsTodayDO.class)
                        .eq(LinkStatsTodayDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkStatsTodayDO::getGid, one.getGid()));
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    linkStatsTodayDOList.forEach(linkStatsTodayDO -> linkStatsTodayDO.setDelFlag(1));
                    linkStatsTodayService.updateBatchById(linkStatsTodayDOList);
                    linkStatsTodayDOList.forEach(each -> {
                        each.setGid(req.getGid());
                        each.setDelFlag(0);
                    });
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }

                linkGotoService.update(new LambdaUpdateWrapper<LinkGotoDO>()
                        .eq(LinkGotoDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkGotoDO::getGid, one.getGid())
                        .set(LinkGotoDO::getDelFlag, 1));
                linkGotoService.save(LinkGotoDO.builder().gid(req.getGid()).fullShortUrl(req.getFullShortUrl()).build());

                LambdaUpdateWrapper<LinkAccessStatsDO> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDO.class)
                        .eq(LinkAccessStatsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkAccessStatsDO::getGid, one.getGid())
                        .eq(LinkAccessStatsDO::getDelFlag, 0);
                LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);

                LambdaUpdateWrapper<LinkLocaleStatsDO> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDO.class)
                        .eq(LinkLocaleStatsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkLocaleStatsDO::getGid, one.getGid())
                        .eq(LinkLocaleStatsDO::getDelFlag, 0);
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDO> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDO.class)
                        .eq(LinkOsStatsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkOsStatsDO::getGid, one.getGid())
                        .eq(LinkOsStatsDO::getDelFlag, 0);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDO> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDO.class)
                        .eq(LinkBrowserStatsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkBrowserStatsDO::getGid, one.getGid())
                        .eq(LinkBrowserStatsDO::getDelFlag, 0);
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDO> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDO.class)
                        .eq(LinkDeviceStatsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkDeviceStatsDO::getGid, one.getGid())
                        .eq(LinkDeviceStatsDO::getDelFlag, 0);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDO> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDO.class)
                        .eq(LinkNetworkStatsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkNetworkStatsDO::getGid, one.getGid())
                        .eq(LinkNetworkStatsDO::getDelFlag, 0);
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDO> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDO.class)
                        .eq(LinkAccessLogsDO::getFullShortUrl, req.getFullShortUrl())
                        .eq(LinkAccessLogsDO::getGid, one.getGid())
                        .eq(LinkAccessLogsDO::getDelFlag, 0);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .gid(req.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);

            } finally {
                rLock.unlock();
            }
        }

        if (Objects.equals(req.getValidDateType(), one.getValidDateType()) &&
                Objects.equals(req.getValidDate(), one.getValidDate())) {
            return;
        }
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, req.getFullShortUrl()));
        if (one.getValidDate() == null || !one.getValidDate().isBefore(LocalDateTime.now())) {
            return;
        }
        if (Objects.equals(req.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType()) || req.getValidDate().isAfter(LocalDateTime.now())) {
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, req.getFullShortUrl()));
        }
    }

    /**
     * 分页查询短链接
     * @param req 分页请求
     * @return 分页结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO req) {
        IPage<ShortLinkDO> page = shortLinkMapper.pageLink(req);
        return page.convert(each -> {
            ShortLinkPageRespDTO bean = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            bean.setDomain("http://" + bean.getDomain());
            return bean;
        });
    }

    /**
     * 查询分组短链接数量
     * @param gIds 分组标识集合
     * @return 分组与其下短链接数量
     */
    @Override
    public List<ShortLinkCountQueryRespDTO> listShortLinkCount(List<String> gIds) {
        // 从表里依据gid查count
        Map<String, List<ShortLinkDO>> gIdMap = list(new LambdaQueryWrapper<ShortLinkDO>()
                .in(!gIds.isEmpty(), ShortLinkDO::getGid, gIds)
                .eq(ShortLinkDO::getEnableStatus, 0))
                .stream().collect(Collectors.groupingBy(ShortLinkDO::getGid));
        return gIdMap.keySet().stream().map(key -> {
            ShortLinkCountQueryRespDTO respDTO = new ShortLinkCountQueryRespDTO();
            respDTO.setGid(key);
            respDTO.setShortLinkCount(gIdMap.get(key).size());
            return respDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 短链接跳转
     * @param shortUri 短链接
     * @param req 请求
     * @param res 响应
     */
    @SneakyThrows
    @Override
    public void jumpLink(String shortUri, ServletRequest req, ServletResponse res) {
        String serverName = req.getServerName();
        String serverPort = Optional.of(req.getServerPort())
                .filter(port -> !Objects.equals(port, 80))
                .map(String::valueOf)
                .map(port -> ":" + port)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        // 查询缓存是否包含原始链接
        String originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalUrl)) {
            shortLinkStats(buildLinkStatsRecordAndSetUser(fullShortUrl, req, res));

            ((HttpServletResponse) res).sendRedirect(originalUrl);
            return;
        }
        // 缓存不包含短链接时，通过布隆过滤器判断数据库中是否包含原数据（即判断是缓存过期，还是无数据）
        if (!shortUriCachePenetrationBloomFilter.contains(shortUri)) {
            // 不包含原数据 直接返回
            ((HttpServletResponse) res).sendRedirect("/page/notfound");
            return;
        }
        // 包含原数据 查看缓存中是否已存储过“空值”，此处指已查询过数据库，且数据库中无对应数据
        if (StrUtil.isNotBlank(stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl)))) {
            ((HttpServletResponse) res).sendRedirect("/page/notfound");
            return;
        }

        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalUrl)) {
                shortLinkStats(buildLinkStatsRecordAndSetUser(fullShortUrl, req, res));
                ((HttpServletResponse) res).sendRedirect(originalUrl);
                return;
            }
            LinkGotoDO one = linkGotoService.getOne(new LambdaQueryWrapper<LinkGotoDO>()
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl));
            if (one == null) {
                // 查数据库为空 填充缓存中空值
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                ((HttpServletResponse) res).sendRedirect("/page/notfound");
                return;
            }
            ShortLinkDO shortLinkDO = getOne(new LambdaQueryWrapper<ShortLinkDO>()
                    .eq(ShortLinkDO::getGid, one.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getEnableStatus, 0));
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().isBefore(LocalDateTime.now()))) {
                // 短链接数据库中有效期已过期
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                ((HttpServletResponse) res).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(), LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
            shortLinkStats(buildLinkStatsRecordAndSetUser(fullShortUrl, req, res));
            ((HttpServletResponse) res).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void shortLinkStats(ShortLinkStatsRecordDTO statsRecord) {
        Map<String, String> prodMap = new HashMap<>();
        prodMap.put("statsRecord", JSON.toJSONString(statsRecord));
        shortLinkStatsSaveProducer.send(prodMap);
    }


    private void verificationWhiteLinkList(String originUrl) {
        if (goToDomainWhiteListConfiguration.getEnabled() == null ||
                !goToDomainWhiteListConfiguration.getEnabled()) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException(SHORT_LINK_ANALYSE_ERROR);
        }
        List<String> details = goToDomainWhiteListConfiguration.getDetails();
        if (details.isEmpty() || !details.contains(domain)) {
            throw new ClientException(SHORT_LINK_PROTECT_ERROR);
        }

    }

    private ShortLinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest req, ServletResponse res) {
        LocalDate today = LocalDate.now();
        Cookie[] cookies = ((HttpServletRequest) req).getCookies();
        AtomicBoolean uvFlag = new AtomicBoolean(false);

        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) res).addCookie(uvCookie);
            uvFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + today + ":" + fullShortUrl, uv.get());
        };

        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies).filter(cookie -> Objects.equals(cookie.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(cookie -> {
                        uv.set(cookie);
                        Long uvAdd = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + today + ":" + fullShortUrl, cookie);
                        uvFlag.set(uvAdd != null && uvAdd > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = ReqUtil.getRealIp(req);
        String os = ReqUtil.getOs(req);
        String browser = ReqUtil.getBrowser(req);
        String device = ReqUtil.getDevice(req);
        String network = ReqUtil.getNetwork(req);
        Long uipAdd = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + today + ":" + fullShortUrl, remoteAddr);
        boolean uipFlag = uipAdd != null && uipAdd > 0L;
        return ShortLinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFlag(uvFlag.get())
                .uipFlag(uipFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
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
            String fullUrl = defaultDomain + "/" + shortUri;
            if (!shortUriCachePenetrationBloomFilter.contains(fullUrl)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    /**
     * 获取目标网站图标
     * @param url 目标网站
     * @return 图标
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl  = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) targetUrl.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.connect();
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            Element first = document.select("link[ref~=(?i)^(shortcut )?icon]").first();
            if (first != null) {
                return first.attr("abs:href");
            }
        }
        return null;
    }

    /**
     * 批量设置 Redis 缓存（使用 Pipeline）
     */
    private void batchSetRedisCache(Map<String, String> cacheMap, LocalDateTime validDate) {
        long expireTime = LinkUtil.getLinkCacheValidTime(validDate);
        long expireSeconds = expireTime / 1000;

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            cacheMap.forEach((key, value) -> {
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);

                // 使用 stringCommands()
                connection.stringCommands().setEx(keyBytes, expireSeconds, valueBytes);
            });
            return null;
        });

        /*long expireTime = LinkUtil.getLinkCacheValidTime(validDate);
        Duration duration = Duration.ofMillis(expireTime);

        // 使用 SessionCallback
        stringRedisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings("unchecked")
            public Object execute(RedisOperations operations) throws DataAccessException {
                cacheMap.forEach((key, value) ->
                        operations.opsForValue().set(key, value, duration)
                );
                return null;
            }
        });*/
    }
}
