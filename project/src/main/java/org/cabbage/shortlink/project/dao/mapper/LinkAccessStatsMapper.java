package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.cabbage.shortlink.project.dao.entity.LinkAccessStatsDO;

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

}
