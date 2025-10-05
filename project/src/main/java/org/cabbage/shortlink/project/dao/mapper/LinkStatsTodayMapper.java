package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.cabbage.shortlink.project.dao.entity.LinkStatsTodayDO;

/**
 * @author xzcabbage
 * @since 2025/10/5
 */
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 插入或更新操作系统访问统计
     * 如果记录存在(根据唯一索引: full_short_url, gid, date)则累加访问量，否则插入新记录
     */
    @Insert("INSERT INTO t_link_stats_today " +
            "(full_short_url, gid, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag) " +
            "VALUES " +
            "(#{fullShortUrl}, #{gid}, #{date}, #{totalPv}, #{totalUv}, #{totalUip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "today_pv = today_pv + #{todayPv}, " +
            "today_uv = today_uv + #{todayUv}, " +
            "today_uip = today_uip + #{todayUip}, " +
            "update_time = NOW()")
    void insertOrUpdate(LinkStatsTodayDO stats);
}
