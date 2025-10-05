package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.cabbage.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.cabbage.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/4
 * 地区统计持久层
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {


    @Insert("INSERT INTO t_link_locale_stats " +
            "(full_short_url, gid, date, cnt, province, city, adcode, country, create_time, update_time, del_flag) " +
            "VALUES " +
            "(#{fullShortUrl}, #{gid}, #{date}, #{cnt}, #{province}, #{city}, #{adcode}, #{country}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{cnt}, " +
            "update_time = NOW()")
    void insertOrUpdate(LinkLocaleStatsDO localeStatsDO);

    /**
     * 查询单个短链接的地域访问 Top5
     */
    @Select("SELECT " +
            "    province, " +
            "    SUM(cnt) AS cnt " +
            "FROM t_link_locale_stats " +
            "WHERE full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND create_time >= #{startDate} " +
            "    AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY full_short_url, gid, province " +
            "ORDER BY cnt DESC " +
            "LIMIT 5")
    List<LinkLocaleStatsDO> queryLocaleTop5BySingleShortLink(ShortLinkStatsReqDTO req);

    /**
     * 查询分组短链接的地域访问 Top5
     */
    @Select("SELECT " +
            "    province, " +
            "    SUM(cnt) AS cnt " +
            "FROM t_link_locale_stats " +
            "WHERE " +
            "    gid = #{gid} " +
            "    AND create_time >= #{startDate} " +
            "    AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY gid, province " +
            "ORDER BY cnt DESC " +
            "LIMIT 5")
    List<LinkLocaleStatsDO> queryLocaleTop5ByGroupShortLink(ShortLinkGroupStatsReqDTO req);

}
