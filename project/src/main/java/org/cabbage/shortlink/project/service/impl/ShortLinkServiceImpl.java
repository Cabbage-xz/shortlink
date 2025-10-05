package org.cabbage.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.common.convention.exception.ServiceException;
import org.cabbage.shortlink.project.common.enums.ValidDateTypeEnum;
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
import org.cabbage.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.cabbage.shortlink.project.service.LinkGotoService;
import org.cabbage.shortlink.project.service.ShortLinkService;
import org.cabbage.shortlink.project.toolkit.HashUtil;
import org.cabbage.shortlink.project.toolkit.LinkUtil;
import org.cabbage.shortlink.project.toolkit.ReqUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_GOTO_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.SHORT_LINK_STATS_UIP_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.SHORT_LINK_STATS_UV_KEY;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_ALREADY_EXIST;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_CREATE_TIMES_TOO_MANY;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_GET_REMOTE_LOCALE_ERROR;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_NOT_EXIST;


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
    private final ShortLinkMapper shortLinkMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAMapKey;

    /**
     * 创建短链接
     *
     * @param req 创建请求实体
     * @return 短链接
     */
    @Transactional
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO req) {
        String shortUri = generateShortUrl(req);
        String fullShortUrl = req.getDomain() + "/" + shortUri;
        ShortLinkDO linkDO = BeanUtil.toBean(req, ShortLinkDO.class);
        linkDO.setShortUri(shortUri);
        linkDO.setFullShortUrl(fullShortUrl);
        linkDO.setFavicon(getFavicon(req.getOriginUrl()));
        linkDO.setTotalPv(0);
        linkDO.setTotalUv(0);
        linkDO.setTotalUip(0);
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
     * 更新短链接
     * @param req 更新请求
     */
    @Transactional
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO req) {
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
                    .eq(ShortLinkDO::getFullShortUrl, req.getFullShortUrl()));
        } else {
            // 分组不同 先删后插
            update(new LambdaUpdateWrapper<ShortLinkDO>()
                    .eq(ShortLinkDO::getGid, req.getOriginGid())
                    .eq(ShortLinkDO::getFullShortUrl, req.getFullShortUrl())
                    .set(ShortLinkDO::getDelFlag, 1));
            linkGotoService.update(new LambdaUpdateWrapper<LinkGotoDO>()
                    .eq(LinkGotoDO::getFullShortUrl, req.getFullShortUrl())
                    .eq(LinkGotoDO::getGid, req.getOriginGid())
                    .set(LinkGotoDO::getDelFlag, 1));
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
            linkGotoService.save(LinkGotoDO.builder().gid(req.getGid()).fullShortUrl(req.getFullShortUrl()).build());
        }
    }

    /**
     * 分页查询短链接
     * @param req 分页请求
     * @return 分页结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO req) {
        IPage<ShortLinkDO> page = page(req, new LambdaQueryWrapper<>(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, req.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .orderByDesc(ShortLinkDO::getCreateTime));
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
        String fullShortUrl = serverName + "/" + shortUri;
        // 查询缓存是否包含原始链接
        String originalUrl = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalUrl)) {
            insertOrUpdateShortLinkStats(fullShortUrl, null, req, res);
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
                insertOrUpdateShortLinkStats(fullShortUrl, null, req, res);
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
            if (shortLinkDO == null || shortLinkDO.getValidDate().isBefore(LocalDateTime.now())) {
                // 短链接数据库中有效期已过期
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.SECONDS);
                ((HttpServletResponse) res).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(), LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
            insertOrUpdateShortLinkStats(fullShortUrl, shortLinkDO.getGid(), req, res);
            ((HttpServletResponse) res).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }

    }

    private void insertOrUpdateShortLinkStats(String fullShortUrl, String gid, ServletRequest req, ServletResponse res) {

        // 获取日期 不包括时分秒
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int weekday = now.getDayOfWeek().getValue();

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

        // 监控基础指标
        String remoteAddr = ReqUtil.getRealIp(req);
        Long uipAdd = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + today + ":" + fullShortUrl, remoteAddr);
        boolean uipFlag = uipAdd != null && uipAdd > 0L;

        if (StrUtil.isBlank(gid)) {
            gid = linkGotoService.getOne(new LambdaQueryWrapper<LinkGotoDO>()
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl))
                    .getGid();
        }


        LinkAccessStatsDO statsDO = LinkAccessStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .hour(hour)
                .weekday(weekday)
                .date(today)
                .pv(1)
                .uv(uvFlag.get() ? 1 : 0)
                .uip(uipFlag ? 1 : 0)
                .build();
        linkAccessStatsMapper.insertOrUpdate(statsDO);

        // 获取地区指标
        Map<String, Object> localeParamMap = new HashMap<>();
        localeParamMap.put("key", statsLocaleAMapKey);
        localeParamMap.put("id", remoteAddr);
        String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
        JSONObject localeResultObj = JSON.parseObject(localeResultStr);
        String infoCode = localeResultObj.getString("infocode");
        if (StrUtil.isBlank(infoCode) || !StrUtil.equals(infoCode, "10000")) {
            throw new ServiceException(SHORT_LINK_GET_REMOTE_LOCALE_ERROR);
        }
        String province = localeResultObj.getString("province");
        boolean unknownFlag = StrUtil.equals(province, "[]");
        LinkLocaleStatsDO localeStatsDO = LinkLocaleStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(today)
                .cnt(1)
                .province(unknownFlag ? "unknown" : province)
                .city(unknownFlag ? "unknown" : localeResultObj.getString("city"))
                .adcode(unknownFlag ? "unknown" : localeResultObj.getString("adcode"))
                .country("China")
                .build();
        linkLocaleStatsMapper.insertOrUpdate(localeStatsDO);

        // 监控操作系统
        LinkOsStatsDO osStatsDO = LinkOsStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(today)
                .cnt(1)
                .os(ReqUtil.getOs(req))
                .build();
        linkOsStatsMapper.insertOrUpdate(osStatsDO);

        // 监控浏览器
        LinkBrowserStatsDO browserStatsDO = LinkBrowserStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(today)
                .cnt(1)
                .browser(ReqUtil.getBrowser(req))
                .build();
        linkBrowserStatsMapper.insertOrUpdate(browserStatsDO);

        // 监控日志
        LinkAccessLogsDO accessLogsDO = LinkAccessLogsDO.builder()
                .user(uv.get())
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .ip(remoteAddr)
                .os(ReqUtil.getOs(req))
                .network(ReqUtil.getNetwork(req))
                .device(ReqUtil.getDevice(req))
                .locale(StrUtil.join("-", "China", province, localeResultObj.getString("city")))
                .browser(ReqUtil.getBrowser(req))
                .build();
        linkAccessLogsMapper.insert(accessLogsDO);

        // 监控设备
        LinkDeviceStatsDO deviceStatsDO = LinkDeviceStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(today)
                .cnt(1)
                .device(ReqUtil.getDevice(req))
                .build();
        linkDeviceStatsMapper.insertOrUpdate(deviceStatsDO);

        // 监控网络
        LinkNetworkStatsDO networkStatsDO = LinkNetworkStatsDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(gid)
                .date(today)
                .cnt(1)
                .network(ReqUtil.getNetwork(req))
                .build();
        linkNetworkStatsMapper.insertOrUpdate(networkStatsDO);


        shortLinkMapper.incrementStats(gid, fullShortUrl, 1, uvFlag.get() ? 1 : 0, uipFlag ? 1 : 0);
        linkStatsTodayMapper.insertOrUpdate(LinkStatsTodayDO.builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(today)
                        .todayPv(1)
                        .todayUv(uvFlag.get() ? 1 : 0)
                        .todayUip(uipFlag ? 1 : 0)
                        .build());
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
}
