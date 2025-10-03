package org.cabbage.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
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
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkGotoDO;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;
import org.cabbage.shortlink.project.dao.mapper.LinkAccessStatsMapper;
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
import org.cabbage.shortlink.project.toolkit.IPUtil;
import org.cabbage.shortlink.project.toolkit.LinkUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.GOTO_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_GOTO_SHORT_LINK_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.SHORT_LINK_STATS_UIP_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.SHORT_LINK_STATS_UV_KEY;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_ALREADY_EXIST;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_CREATE_TIMES_TOO_MANY;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_NOT_EXIST;


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

    private final LinkGotoService linkGotoService;
    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

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

        Cookie[] cookies = ((HttpServletRequest) req).getCookies();
        AtomicBoolean uvFlag = new AtomicBoolean(false);

        Runnable addResponseCookieTask = () -> {
            String uv = UUID.fastUUID().toString();
            Cookie uvCookie = new Cookie("uv", uv);
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) res).addCookie(uvCookie);
            uvFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv);
        };

        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies).filter(cookie -> Objects.equals(cookie.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(cookie -> {
                        Long uvAdd = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, cookie);
                        uvFlag.set(uvAdd != null && uvAdd > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }

        String remoteAddr = IPUtil.getRealIp(req);
        Long uipAdd = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFlag = uipAdd != null && uipAdd > 0L;


        if (StrUtil.isBlank(gid)) {
            gid = linkGotoService.getOne(new LambdaQueryWrapper<LinkGotoDO>()
                    .eq(LinkGotoDO::getFullShortUrl, fullShortUrl))
                    .getGid();
        }

        // 获取日期 不包括时分秒
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int weekday = now.getDayOfWeek().getValue();
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
