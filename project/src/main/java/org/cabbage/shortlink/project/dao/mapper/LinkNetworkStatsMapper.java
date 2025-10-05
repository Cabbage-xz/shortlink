package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.cabbage.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.cabbage.shortlink.project.dto.req.ShortLinkStatsReqDTO;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 插入或更新操作系统访问统计
     * 如果记录存在(根据唯一索引: full_short_url, gid, network)则累加访问量，否则插入新记录
     */
    @Insert("INSERT INTO t_link_network_stats " +
            "(full_short_url, gid, date, cnt, netwrok, create_time, update_time, del_flag) " +
            "VALUES " +
            "(#{fullShortUrl}, #{gid}, #{date}, #{cnt}, #{netwrok}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{cnt}, " +
            "update_time = NOW()")
    void insertOrUpdate(LinkNetworkStatsDO stats);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    network, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_network_stats " +
            "WHERE " +
            "    full_short_url = #{fullShortUrl} " +
            "    AND gid = #{gid} " +
            "    AND date BETWEEN #{startDate} and #{endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, network;")
    List<LinkNetworkStatsDO> queryNetworkStatsBySingleShortLink(ShortLinkStatsReqDTO req);
}
