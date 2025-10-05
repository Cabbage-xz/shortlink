package org.cabbage.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.cabbage.shortlink.project.dao.entity.ShortLinkDO;

/**
 * @author xzcabbage
 * @since 2025/9/24
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    /**
     * 短链接访问统计自增
     */
    @Update("UPDATE " +
            "   t_link " +
            "SET " +
            "    total_pv = total_pv + #{totalPv}, " +
            "    total_uv = total_uv + #{totalUv}, " +
            "    total_uip = total_uip + #{totalUip} " +
            "WHERE " +
            "    gid = #{gid} " +
            "AND full_short_url = #{fullShortUrl}")
    void incrementStats(@Param("gid") String gid,
                        @Param("fullShortUrl") String fullShortUrl,
                        @Param("totalPv") int totalPv,
                        @Param("totalUv") int totalUv,
                        @Param("totalUip") int totalUip);
}
