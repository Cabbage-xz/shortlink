package org.cabbage.shortlink.project.mq.consumer;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.common.convention.exception.ServiceException;
import org.cabbage.shortlink.project.dao.entity.LinkAccessLogsDO;
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkBrowserStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkDeviceStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkGotoDO;
import org.cabbage.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkOsStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkStatsTodayDO;
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
import org.cabbage.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import org.cabbage.shortlink.project.service.LinkGotoService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_GID_UPDATE_KEY;
import static org.cabbage.shortlink.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
import static org.cabbage.shortlink.project.common.enums.ShortLInkErrorCodeEnum.SHORT_LINK_GET_REMOTE_LOCALE_ERROR;

/**
 * @author xzcabbage
 * @since 2025/10/8
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShortLinkStatsSaveConsumer implements StreamListener<String, MapRecord<String, String, String>> {



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

    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;

    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;



    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocalAmapKey;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String stream = message.getStream();
        RecordId id = message.getId();
        Map<String, String> prodMap = message.getValue();
        String fullShortUrl = prodMap.get("fullShortUrl");
        String gid = prodMap.get("gid");
        ShortLinkStatsRecordDTO statsRecord = JSON.parseObject(prodMap.get("statsRecord"), ShortLinkStatsRecordDTO.class);
        actualSaveShortLinkStats(fullShortUrl, gid, statsRecord);

        stringRedisTemplate.opsForStream().delete(Objects.requireNonNull(stream), id.getValue());
    }

    public void actualSaveShortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO statsRecord) {
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = rwLock.readLock();
        if (!rLock.tryLock()) {
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try {
            if (StrUtil.isBlank(gid)) {
                gid = linkGotoService.getOne(new LambdaQueryWrapper<LinkGotoDO>()
                        .eq(LinkGotoDO::getFullShortUrl, fullShortUrl)).getGid();
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
                    .uv(statsRecord.getUvFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFlag() ? 1 : 0)
                    .build();
            linkAccessStatsMapper.insertOrUpdate(statsDO);

            // 获取地区指标
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocalAmapKey);
            localeParamMap.put("ip", statsRecord.getRemoteAddr());
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infoCode = localeResultObj.getString("infocode");
            if (StrUtil.isBlank(infoCode) || !StrUtil.equals(infoCode, "10000")) {
                throw new ServiceException(SHORT_LINK_GET_REMOTE_LOCALE_ERROR);
            }
            String provinceStr = localeResultObj.getString("province");
            boolean unknownFlag = StrUtil.equals(provinceStr, "[]");
            String actualProvince = unknownFlag ? "unknown" : provinceStr;
            String actualCity = unknownFlag ? "unknown" : localeResultObj.getString("city");

            LinkLocaleStatsDO localeStatsDO = LinkLocaleStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(today)
                    .cnt(1)
                    .province(actualProvince)
                    .city(actualCity)
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
                    .os(statsRecord.getOs())
                    .build();
            linkOsStatsMapper.insertOrUpdate(osStatsDO);

            // 监控浏览器
            LinkBrowserStatsDO browserStatsDO = LinkBrowserStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(today)
                    .cnt(1)
                    .browser(statsRecord.getBrowser())
                    .build();
            linkBrowserStatsMapper.insertOrUpdate(browserStatsDO);

            // 监控日志
            LinkAccessLogsDO accessLogsDO = LinkAccessLogsDO.builder()
                    .user(statsRecord.getUv())
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .ip(statsRecord.getRemoteAddr())
                    .os(statsRecord.getOs())
                    .network(statsRecord.getNetwork())
                    .device(statsRecord.getDevice())
                    .locale(StrUtil.join("-", "China", actualProvince, actualCity))
                    .browser(statsRecord.getBrowser())
                    .build();
            linkAccessLogsMapper.insert(accessLogsDO);

            // 监控设备
            LinkDeviceStatsDO deviceStatsDO = LinkDeviceStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(today)
                    .cnt(1)
                    .device(statsRecord.getDevice())
                    .build();
            linkDeviceStatsMapper.insertOrUpdate(deviceStatsDO);

            // 监控网络
            LinkNetworkStatsDO networkStatsDO = LinkNetworkStatsDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(today)
                    .cnt(1)
                    .network(statsRecord.getNetwork())
                    .build();
            linkNetworkStatsMapper.insertOrUpdate(networkStatsDO);


            shortLinkMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFlag() ? 1 : 0, statsRecord.getUipFlag() ? 1 : 0);
            linkStatsTodayMapper.insertOrUpdate(LinkStatsTodayDO.builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(today)
                    .todayPv(1)
                    .todayUv(statsRecord.getUvFlag() ? 1 : 0)
                    .todayUip(statsRecord.getUipFlag() ? 1 : 0)
                    .build());
        } catch (Exception e) {
            log.error("短链接访问量统计异常", e);
        } finally {
            rLock.unlock();
        }
    }
}
