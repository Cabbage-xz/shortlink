package org.cabbage.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.admin.dao.entity.GroupDO;
import org.cabbage.shortlink.admin.dao.mapper.GroupMapper;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.springframework.stereotype.Service;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接分组接口层实现类
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
}
