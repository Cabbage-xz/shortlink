package org.cabbage.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.project.dao.bo.ShortLinkStatsAccessLogBO;
import org.cabbage.shortlink.project.dao.entity.LinkAccessLogsDO;
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkBrowserStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkDeviceStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.cabbage.shortlink.project.dao.entity.LinkOsStatsDO;
import org.cabbage.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkBrowserStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkDeviceStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkNetworkStatsMapper;
import org.cabbage.shortlink.project.dao.mapper.LinkOsStatsMapper;
import org.cabbage.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsAccessDailyRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsBrowserRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsDeviceRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsLocaleCNRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsNetworkRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsOsRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsTopIpRespDTO;
import org.cabbage.shortlink.project.dto.resp.ShortLinkStatsUvRespDTO;
import org.cabbage.shortlink.project.service.ShortLinkStatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    /**
     * 监控单个短链接使用情况
     * @param req 监控请求
     * @return 使用情况
     */
    @Override
    public ShortLinkStatsRespDTO singleShortLinkStats(ShortLinkStatsReqDTO req) {
        List<LinkAccessStatsDO> linkAccessStatsDOS = linkAccessStatsMapper.queryStatsBySingleShortLink(req);
        if (linkAccessStatsDOS.isEmpty()) {
            return null;
        }

        LinkAccessStatsDO pvUvUipStatsBySingleShortLink = linkAccessLogsMapper.findPvUvUipStatsBySingleShortLink(req);
        int totalPv = pvUvUipStatsBySingleShortLink.getPv();
        int totalUv = pvUvUipStatsBySingleShortLink.getUv();
        int totalUip = pvUvUipStatsBySingleShortLink.getUip();

        // 查询请求范围内是否有相关使用记录
        Map<LocalDate, ShortLinkStatsAccessDailyRespDTO> shortLinkStatsAccessDateMap = linkAccessStatsDOS
                .stream()
                .map(linkAccessStatsDO -> BeanUtil.copyProperties(linkAccessStatsDO, ShortLinkStatsAccessDailyRespDTO.class))
                .collect(Collectors.toMap(ShortLinkStatsAccessDailyRespDTO::getDate,
                        e -> e,
                        (e1, e2) -> e1));

        // resp中daily数据填充
        List<ShortLinkStatsAccessDailyRespDTO> dailyStats = req.getStartDate().datesUntil(req.getEndDate().plusDays(1))
                .map(date -> shortLinkStatsAccessDateMap.getOrDefault(date, ShortLinkStatsAccessDailyRespDTO.builder()
                        .date(date)
                        .pv(0)
                        .uv(0)
                        .uip(0)
                        .build()))
                .toList();

        // resp中地区数据填充
        List<LinkLocaleStatsDO> localeStatsDOList = linkLocaleStatsMapper.queryLocaleTop5BySingleShortLink(req);
        int localeCntSum = localeStatsDOList.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        List<ShortLinkStatsLocaleCNRespDTO> localeStats = localeStatsDOList.stream().map(localeDo ->
                ShortLinkStatsLocaleCNRespDTO.builder()
                        .locale(localeDo.getProvince())
                        .cnt(localeDo.getCnt())
                        .ratio(Math.round(((double) localeDo.getCnt() / localeCntSum) * 100.0) / 100.0)
                        .build()
        ).toList();

        // 小时访问详情
        Map<Integer, LinkAccessStatsDO> shortLinkStatsAccessHourMap = linkAccessStatsMapper.queryHourStatsBySingleShortLink(req)
                .stream().collect(Collectors.toMap(LinkAccessStatsDO::getHour, e -> e));
        Map<Integer, Integer> hourStats = new HashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            hourStats.put(hour, shortLinkStatsAccessHourMap.getOrDefault(hour, LinkAccessStatsDO.builder().pv(0).build()).getPv());
        }

        // 一周访问详情
        Map<Integer, LinkAccessStatsDO> shortLinkStatsAccessWeekdayMap = linkAccessStatsMapper.queryWeekdayStatsBySingleShortLink(req)
                .stream().collect(Collectors.toMap(LinkAccessStatsDO::getWeekday, e -> e));
        Map<Integer, Integer> weekdayStats = new HashMap<>();
        for (int weekday = 1; weekday < 8; weekday++) {
            weekdayStats.put(weekday, shortLinkStatsAccessWeekdayMap.getOrDefault(weekday, LinkAccessStatsDO.builder().pv(0).build()).getPv());
        }

        // 高频IP访问详情
        List<ShortLinkStatsAccessLogBO> ShortLinkTop5Ip = linkAccessLogsMapper.listTop5IpByShortLink(req);
        List<ShortLinkStatsTopIpRespDTO> topIpStats = BeanUtil.copyToList(ShortLinkTop5Ip, ShortLinkStatsTopIpRespDTO.class);

        // 访客类型访问详情
        ShortLinkStatsAccessLogBO uvTypeCntByShortLink = linkAccessLogsMapper.findUvTypeCntByShortLink(req);
        int oldUserCnt = uvTypeCntByShortLink.getCnt();
        int newUserCnt = uvTypeCntByShortLink.getCnt();
        int totalUserCnt = oldUserCnt + newUserCnt;

        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        uvTypeStats.add(ShortLinkStatsUvRespDTO.builder().uvType("newUser").cnt(newUserCnt)
                .ratio(Math.round(((double) newUserCnt / totalUserCnt) * 100.0) / 100.0).build());
        uvTypeStats.add(ShortLinkStatsUvRespDTO.builder().uvType("oldUser").cnt(oldUserCnt)
                .ratio(Math.round(((double) oldUserCnt / totalUserCnt) * 100.0) / 100.0).build());

        // 浏览器访问详情
        List<LinkBrowserStatsDO> linkBrowserStatsDOS = linkBrowserStatsMapper.queryBrowserStatsBySingleShortLink(req);
        int browserSum = linkBrowserStatsDOS.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        List<ShortLinkStatsBrowserRespDTO> browserStats = linkBrowserStatsDOS.stream()
                .collect(Collectors.toMap(LinkBrowserStatsDO::getBrowser, LinkBrowserStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / browserSum) * 100.0) / 100.0;
                    return ShortLinkStatsBrowserRespDTO.builder()
                            .browser(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        // 操作系统访问详情
        List<LinkOsStatsDO> linkOsStatsDOS = linkOsStatsMapper.queryOsStatsBySingleShortLink(req);
        int osSum = linkOsStatsDOS.stream().mapToInt(LinkOsStatsDO::getCnt).sum();
        List<ShortLinkStatsOsRespDTO> osStats = linkOsStatsDOS.stream()
                .collect(Collectors.toMap(LinkOsStatsDO::getOs, LinkOsStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / osSum) * 100.0) / 100.0;
                    return ShortLinkStatsOsRespDTO.builder()
                            .os(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        // 设备类型访问详情
        List<LinkDeviceStatsDO> linkDeviceStatsDOS = linkDeviceStatsMapper.queryDeviceStatsBySingleShortLink(req);
        int deviceSum = linkDeviceStatsDOS.stream().mapToInt(LinkDeviceStatsDO::getCnt).sum();
        List<ShortLinkStatsDeviceRespDTO> deviceStats = linkDeviceStatsDOS.stream()
                .collect(Collectors.toMap(LinkDeviceStatsDO::getDevice, LinkDeviceStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / deviceSum) * 100.0) / 100.0;
                    return ShortLinkStatsDeviceRespDTO.builder()
                            .device(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        // 网络类型访问详情
        List<LinkNetworkStatsDO> linkNetworkStatsDOS = linkNetworkStatsMapper.queryNetworkStatsBySingleShortLink(req);
        int networkSum = linkNetworkStatsDOS.stream().mapToInt(LinkNetworkStatsDO::getCnt).sum();
        List<ShortLinkStatsNetworkRespDTO> networkStats = linkNetworkStatsDOS.stream()
                .collect(Collectors.toMap(LinkNetworkStatsDO::getNetwork, LinkNetworkStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / networkSum) * 100.0) / 100.0;
                    return ShortLinkStatsNetworkRespDTO.builder()
                            .network(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        return ShortLinkStatsRespDTO.builder()
                .pv(totalPv)
                .uv(totalUv)
                .uip(totalUip)
                .daily(dailyStats)
                .localeCnStats(localeStats)
                .hourStats(hourStats)
                .weekdayStats(weekdayStats)
                .topIpStats(topIpStats)
                .uvTypeStats(uvTypeStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .build();
    }

    /**
     * 监控分组短链接使用情况
     * @param req 监控请求
     * @return 使用情况
     */
    @Override
    public ShortLinkStatsRespDTO groupShortLinkStats(ShortLinkGroupStatsReqDTO req) {
        List<LinkAccessStatsDO> linkAccessStatsDOS = linkAccessStatsMapper.queryStatsByGroupShortLink(req);
        if (linkAccessStatsDOS.isEmpty()) {
            return null;
        }

        LinkAccessStatsDO pvUvUipStatsBySingleShortLink = linkAccessLogsMapper.findPvUvUipStatsByGroupShortLink(req);
        int totalPv = pvUvUipStatsBySingleShortLink.getPv();
        int totalUv = pvUvUipStatsBySingleShortLink.getUv();
        int totalUip = pvUvUipStatsBySingleShortLink.getUip();

        // 查询请求范围内是否有相关使用记录
        Map<LocalDate, ShortLinkStatsAccessDailyRespDTO> shortLinkStatsAccessDateMap = linkAccessStatsDOS
                .stream()
                .map(linkAccessStatsDO -> BeanUtil.copyProperties(linkAccessStatsDO, ShortLinkStatsAccessDailyRespDTO.class))
                .collect(Collectors.toMap(ShortLinkStatsAccessDailyRespDTO::getDate,
                        e -> e,
                        (e1, e2) -> e1));

        // resp中daily数据填充
        List<ShortLinkStatsAccessDailyRespDTO> dailyStats = req.getStartDate().datesUntil(req.getEndDate().plusDays(1))
                .map(date -> shortLinkStatsAccessDateMap.getOrDefault(date, ShortLinkStatsAccessDailyRespDTO.builder()
                        .date(date)
                        .pv(0)
                        .uv(0)
                        .uip(0)
                        .build()))
                .toList();

        // resp中地区数据填充
        List<LinkLocaleStatsDO> localeStatsDOList = linkLocaleStatsMapper.queryLocaleTop5ByGroupShortLink(req);
        int localeCntSum = localeStatsDOList.stream().mapToInt(LinkLocaleStatsDO::getCnt).sum();
        List<ShortLinkStatsLocaleCNRespDTO> localeStats = localeStatsDOList.stream().map(localeDo ->
                ShortLinkStatsLocaleCNRespDTO.builder()
                        .locale(localeDo.getProvince())
                        .cnt(localeDo.getCnt())
                        .ratio(Math.round(((double) localeDo.getCnt() / localeCntSum) * 100.0) / 100.0)
                        .build()
        ).toList();

        // 小时访问详情
        Map<Integer, LinkAccessStatsDO> shortLinkStatsAccessHourMap = linkAccessStatsMapper.queryHourStatsByGroupShortLink(req)
                .stream().collect(Collectors.toMap(LinkAccessStatsDO::getHour, e -> e));
        Map<Integer, Integer> hourStats = new HashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            hourStats.put(hour, shortLinkStatsAccessHourMap.getOrDefault(hour, LinkAccessStatsDO.builder().pv(0).build()).getPv());
        }

        // 一周访问详情
        Map<Integer, LinkAccessStatsDO> shortLinkStatsAccessWeekdayMap = linkAccessStatsMapper.queryWeekdayStatsByGroupShortLink(req)
                .stream().collect(Collectors.toMap(LinkAccessStatsDO::getWeekday, e -> e));
        Map<Integer, Integer> weekdayStats = new HashMap<>();
        for (int weekday = 1; weekday < 8; weekday++) {
            weekdayStats.put(weekday, shortLinkStatsAccessWeekdayMap.getOrDefault(weekday, LinkAccessStatsDO.builder().pv(0).build()).getPv());
        }

        // 高频IP访问详情
        List<ShortLinkStatsAccessLogBO> ShortLinkTop5Ip = linkAccessLogsMapper.listTop5IpByGroupShortLink(req);
        List<ShortLinkStatsTopIpRespDTO> topIpStats = BeanUtil.copyToList(ShortLinkTop5Ip, ShortLinkStatsTopIpRespDTO.class);

        // 访客类型访问详情
        ShortLinkStatsAccessLogBO uvTypeCntByShortLink = linkAccessLogsMapper.findUvTypeCntByGroupShortLink(req);
        int oldUserCnt = uvTypeCntByShortLink.getCnt();
        int newUserCnt = uvTypeCntByShortLink.getCnt();
        int totalUserCnt = oldUserCnt + newUserCnt;

        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        uvTypeStats.add(ShortLinkStatsUvRespDTO.builder().uvType("newUser").cnt(newUserCnt)
                .ratio(Math.round(((double) newUserCnt / totalUserCnt) * 100.0) / 100.0).build());
        uvTypeStats.add(ShortLinkStatsUvRespDTO.builder().uvType("oldUser").cnt(oldUserCnt)
                .ratio(Math.round(((double) oldUserCnt / totalUserCnt) * 100.0) / 100.0).build());

        // 浏览器访问详情
        List<LinkBrowserStatsDO> linkBrowserStatsDOS = linkBrowserStatsMapper.queryBrowserStatsByGroupShortLink(req);
        int browserSum = linkBrowserStatsDOS.stream().mapToInt(LinkBrowserStatsDO::getCnt).sum();
        List<ShortLinkStatsBrowserRespDTO> browserStats = linkBrowserStatsDOS.stream()
                .collect(Collectors.toMap(LinkBrowserStatsDO::getBrowser, LinkBrowserStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / browserSum) * 100.0) / 100.0;
                    return ShortLinkStatsBrowserRespDTO.builder()
                            .browser(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        // 操作系统访问详情
        List<LinkOsStatsDO> linkOsStatsDOS = linkOsStatsMapper.queryOsStatsByGroupShortLink(req);
        int osSum = linkOsStatsDOS.stream().mapToInt(LinkOsStatsDO::getCnt).sum();
        List<ShortLinkStatsOsRespDTO> osStats = linkOsStatsDOS.stream()
                .collect(Collectors.toMap(LinkOsStatsDO::getOs, LinkOsStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / osSum) * 100.0) / 100.0;
                    return ShortLinkStatsOsRespDTO.builder()
                            .os(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        // 设备类型访问详情
        List<LinkDeviceStatsDO> linkDeviceStatsDOS = linkDeviceStatsMapper.queryDeviceStatsByGroupShortLink(req);
        int deviceSum = linkDeviceStatsDOS.stream().mapToInt(LinkDeviceStatsDO::getCnt).sum();
        List<ShortLinkStatsDeviceRespDTO> deviceStats = linkDeviceStatsDOS.stream()
                .collect(Collectors.toMap(LinkDeviceStatsDO::getDevice, LinkDeviceStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / deviceSum) * 100.0) / 100.0;
                    return ShortLinkStatsDeviceRespDTO.builder()
                            .device(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        // 网络类型访问详情
        List<LinkNetworkStatsDO> linkNetworkStatsDOS = linkNetworkStatsMapper.queryNetworkStatsByGroupShortLink(req);
        int networkSum = linkNetworkStatsDOS.stream().mapToInt(LinkNetworkStatsDO::getCnt).sum();
        List<ShortLinkStatsNetworkRespDTO> networkStats = linkNetworkStatsDOS.stream()
                .collect(Collectors.toMap(LinkNetworkStatsDO::getNetwork, LinkNetworkStatsDO::getCnt))
                .entrySet().stream()
                .map(entry -> {
                    double ratio = Math.round(((double) entry.getValue() / networkSum) * 100.0) / 100.0;
                    return ShortLinkStatsNetworkRespDTO.builder()
                            .network(entry.getKey())
                            .cnt(entry.getValue())
                            .ratio(ratio)
                            .build();
                }).toList();

        return ShortLinkStatsRespDTO.builder()
                .pv(totalPv)
                .uv(totalUv)
                .uip(totalUip)
                .daily(dailyStats)
                .localeCnStats(localeStats)
                .hourStats(hourStats)
                .weekdayStats(weekdayStats)
                .topIpStats(topIpStats)
                .uvTypeStats(uvTypeStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .build();
    }


    /**
     * 监控单个短链接访问记录
     * @param req 监控访问请求
     * @return 访问情况
     */
    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLInkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO req) {
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(req, new LambdaQueryWrapper<LinkAccessLogsDO>()
                .eq(LinkAccessLogsDO::getFullShortUrl, req.getFullShortUrl())
                .eq(LinkAccessLogsDO::getGid, req.getGid())
                .between(LinkAccessLogsDO::getCreateTime, req.getStartDate(), req.getEndDate().plusDays(1)));
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage
                .convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
        if (actualResult.getRecords().isEmpty()) {
            return actualResult;
        }
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser).toList();
        List<Map<String, Object>> uvTypeList = linkAccessLogsMapper.selectUvTypeByUsers(
                req.getGid(), req.getFullShortUrl(), req.getStartDate(),
                req.getEndDate().plusDays(1), userAccessLogsList);
        actualResult.getRecords().forEach(record -> {
            String uvType = uvTypeList.stream()
                    .filter(item -> Objects.equals(item.get("user"), record.getUser()))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            record.setUvType(uvType);
        });
        return actualResult;

    }

    /**
     * 监控分组短链接访问记录
     * @param req 监控访问请求
     * @return 访问情况
     */
    @Override
    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLInkGroupStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO req) {
        IPage<LinkAccessLogsDO> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(req, new LambdaQueryWrapper<LinkAccessLogsDO>()
                .eq(LinkAccessLogsDO::getGid, req.getGid())
                .between(LinkAccessLogsDO::getCreateTime, req.getStartDate(), req.getEndDate().plusDays(1)));
        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage
                .convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
        if (actualResult.getRecords().isEmpty()) {
            return actualResult;
        }
        List<String> userAccessLogsList = actualResult.getRecords().stream()
                .map(ShortLinkStatsAccessRecordRespDTO::getUser).toList();
        List<Map<String, Object>> uvTypeList = linkAccessLogsMapper.selectGroupUvTypeByUsers(
                req.getGid(), req.getStartDate(),
                req.getEndDate().plusDays(1), userAccessLogsList);
        actualResult.getRecords().forEach(record -> {
            String uvType = uvTypeList.stream()
                    .filter(item -> Objects.equals(item.get("user"), record.getUser()))
                    .findFirst()
                    .map(item -> item.get("uvType"))
                    .map(Object::toString)
                    .orElse("旧访客");
            record.setUvType(uvType);
        });
        return actualResult;
    }
}
