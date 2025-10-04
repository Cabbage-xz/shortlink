package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.cabbage.shortlink.project.dao.entity.LinkDeviceStatsDO;

/**
 * @author xzcabbage
 * @since 2025/10/4
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {

    /**
     * 插入或更新操作系统访问统计
     * 如果记录存在(根据唯一索引: full_short_url, gid, date, device)则累加访问量，否则插入新记录
     */
    @Insert("INSERT INTO t_link_os_stats " +
            "(full_short_url, gid, date, cnt, device, create_time, update_time, del_flag) " +
            "VALUES " +
            "(#{fullShortUrl}, #{gid}, #{date}, #{cnt}, #{device}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{cnt}, " +
            "update_time = NOW()")
    void insertOrUpdate(LinkDeviceStatsDO stats);
}
