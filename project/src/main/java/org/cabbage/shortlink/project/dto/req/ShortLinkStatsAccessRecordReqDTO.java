package org.cabbage.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.cabbage.shortlink.project.dao.entity.LinkAccessLogsDO;

import java.time.LocalDate;

/**
 * @author xzcabbage
 * @since 2025/10/5
 */
@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogsDO> {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;
}
