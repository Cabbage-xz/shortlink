package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.cabbage.shortlink.project.dao.entity.LinkOsStatsDO;
import org.cabbage.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    /**
     * 插入或更新操作系统访问统计
     * 如果记录存在(根据唯一索引: full_short_url, gid, os)则累加访问量，否则插入新记录
     */
    @Insert("INSERT INTO t_link_os_stats " +
            "(full_short_url, gid, date, cnt, os, create_time, update_time, del_flag) " +
            "VALUES " +
            "(#{fullShortUrl}, #{gid}, #{date}, #{cnt}, #{os}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{cnt}, " +
            "update_time = NOW()")
    void insertOrUpdate(LinkOsStatsDO stats);


    /**
     * 根据短链接获取指定日期内操作系统监控数据
     */
    @Select("SELECT " +
            "    os, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_os_stats " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND create_time >= #{startDate} " +
            "    AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "    full_short_url, gid, os;")
    List<LinkOsStatsDO> queryOsStatsBySingleShortLink(ShortLinkStatsReqDTO req);

    /**
     * 根据分组短链接获取指定日期内操作系统监控数据
     */
    @Select("SELECT " +
            "    os, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_os_stats " +
            "WHERE " +
            "    gid = #{gid} " +
            "    AND create_time >= #{startDate} " +
            "    AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "    gid, os;")
    List<LinkOsStatsDO> queryOsStatsByGroupShortLink(ShortLinkGroupStatsReqDTO req);
}
