package org.cabbage.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cabbage.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.cabbage.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import org.cabbage.shortlink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

/**
 * @author xzcabbage
 * @since 2025/10/6
 */
@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO>
        implements LinkStatsTodayService {
}
