package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.cabbage.shortlink.project.dao.bo.ShortLinkStatsAccessLogBO;
import org.cabbage.shortlink.project.dao.entity.LinkAccessLogsDO;
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.cabbage.shortlink.common.dto.req.ShortLinkGroupStatsReqDTO;
import org.cabbage.shortlink.common.dto.req.ShortLinkStatsReqDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
public interface LinkAccessLogsMapper extends BaseMapper<LinkAccessLogsDO> {

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    @Select("SELECT " +
            "    ip, " +
            "    COUNT(ip) AS cnt " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND create_time >= #{startDate} " +
            "    AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "    full_short_url, gid, ip " +
            "ORDER BY " +
            "    count DESC " +
            "LIMIT 5;")
    List<ShortLinkStatsAccessLogBO> listTop5IpByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组短链接获取指定日期内高频访问IP数据
     */
    @Select("SELECT " +
            "    ip, " +
            "    COUNT(ip) AS cnt " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND create_time >= #{startDate} " +
            "    AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "    gid, ip " +
            "ORDER BY " +
            "    count DESC " +
            "LIMIT 5;")
    List<ShortLinkStatsAccessLogBO> listTop5IpByGroupShortLink(ShortLinkGroupStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     */
    @Select("SELECT " +
            "    SUM(old_user) AS oldUserCnt, " +
            "    SUM(new_user) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{startDate} AND MAX(create_time) <= #{endDate} THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link_access_logs " +
            "    WHERE " +
            "        full_short_url = #{fullShortUrl} " +
            "        AND gid = #{gid} " +
            "    GROUP BY " +
            "        user " +
            ") AS user_counts;")
    ShortLinkStatsAccessLogBO findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组短链接获取指定日期内新旧访客数据
     */
    @Select("SELECT " +
            "    SUM(old_user) AS oldUserCnt, " +
            "    SUM(new_user) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{startDate} AND MAX(create_time) <= #{endDate} THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link_access_logs " +
            "    WHERE " +
            "        gid = #{gid} " +
            "    GROUP BY " +
            "        user " +
            ") AS user_counts;")
    ShortLinkStatsAccessLogBO findUvTypeCntByGroupShortLink(@Param("param") ShortLinkGroupStatsReqDTO requestParam);


    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>"
    )
    List<Map<String, Object>> selectUvTypeByUsers(@Param("gid") String gid,
                                                  @Param("fullShortUrl") String fullShortUrl,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("userAccessLogsList") List<String> userAccessLogsList);

    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{startDate} AND #{endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    gid = #{gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='userAccessLogsList' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>"
    )
    List<Map<String, Object>> selectGroupUvTypeByUsers(@Param("gid") String gid,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("userAccessLogsList") List<String> userAccessLogsList);

    @Select("SELECT " +
            "   COUNT(user) AS pv, " +
            "   COUNT(DISTINCT user) AS uv, " +
            "   COUNT(DISTINCT ip) AS uip " +
            "FROM " +
            "   t_link_access_logs " +
            "WHERE " +
            "   full_short_url = #{fullShortUrl} " +
            "   AND gid = #{gid} " +
            "   AND create_time >= #{startDate} " +
            "   AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "   full_short_url, gid;")
    LinkAccessStatsDO findPvUvUipStatsBySingleShortLink(ShortLinkStatsReqDTO req);

    @Select("SELECT " +
            "   gid, " +
            "   COUNT(user) AS pv, " +
            "   COUNT(DISTINCT user) AS uv, " +
            "   COUNT(DISTINCT ip) AS uip " +
            "FROM " +
            "   t_link_access_logs " +
            "WHERE " +
            "   gid = #{gid} " +
            "   AND create_time >= #{startDate} " +
            "   AND create_time < DATE_ADD(#{endDate}, INTERVAL 1 DAY) " +
            "GROUP BY " +
            "   gid;")
    LinkAccessStatsDO findPvUvUipStatsByGroupShortLink(ShortLinkGroupStatsReqDTO req);
}
