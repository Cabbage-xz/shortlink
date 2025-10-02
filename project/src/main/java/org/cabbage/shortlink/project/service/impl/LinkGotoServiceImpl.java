package org.cabbage.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cabbage.shortlink.project.dao.entity.LinkGotoDO;
import org.cabbage.shortlink.project.dao.mapper.LinkGotoMapper;
import org.cabbage.shortlink.project.service.LinkGotoService;
import org.springframework.stereotype.Service;

/**
 * @author xzcabbage
 * @since 2025/10/2
 * 短链接跳转接口层
 */
@Service
public class LinkGotoServiceImpl extends ServiceImpl<LinkGotoMapper, LinkGotoDO> implements LinkGotoService {
}
