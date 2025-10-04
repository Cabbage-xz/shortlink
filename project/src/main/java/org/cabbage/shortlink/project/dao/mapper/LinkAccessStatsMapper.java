package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 短链接基础监控持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    @Insert("INSERT INTO t_link_access_stats " +
            "(full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag) " +
            "VALUES " +
            "(#{fullShortUrl}, #{gid}, #{date}, #{pv}, #{uv}, #{uip}, #{hour}, #{weekday}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "pv = pv + #{pv}, " +
            "uv = uv + #{uv}, " +
            "uip = uip + #{uip}, " +
            "update_time = NOW()")
    void insertOrUpdate(LinkAccessStatsDO stats);


    /**
     * 查询指定日期范围内 按天划分 基础监控数据 基础数据直接求和
     * @param req 单链接基础数据查询
     * @return 查询结果
     */
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND date BETWEEN #{startDate} and #{endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date;")
    List<LinkAccessStatsDO> queryStatsBySingleShortLink(ShortLinkStatsReqDTO req);


    /**
     * 查询指定日期范围内 按小时划分 基础监控数据 基础数据直接求和
     * @param req 单链接基础数据查询
     * @return 查询结果
     */
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND date BETWEEN #{startDate} and #{endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, hour;")
    List<LinkAccessStatsDO> queryHourStatsBySingleShortLink(ShortLinkStatsReqDTO req);


    /**
     * 查询指定日期范围内 按星期划分 基础监控数据 基础数据直接求和
     * @param req 单链接基础数据查询
     * @return 查询结果
     */
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND date BETWEEN #{startDate} and #{endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, weekday;")
    List<LinkAccessStatsDO> queryWeekdayStatsBySingleShortLink(ShortLinkStatsReqDTO req);

}
