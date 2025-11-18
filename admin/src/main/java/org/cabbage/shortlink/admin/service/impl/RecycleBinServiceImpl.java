package org.cabbage.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.biz.user.UserContext;
import org.cabbage.shortlink.admin.dao.entity.GroupDO;
import org.cabbage.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.cabbage.shortlink.admin.service.interfaces.RecycleBinService;
import org.cabbage.shortlink.common.convention.exception.ServiceException;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.cabbage.shortlink.common.dto.resp.ShortLinkPageRespDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.cabbage.shortlink.admin.common.enums.GroupErrorCodeEnum.GROUP_NOT_EXISTS;

/**
 * @author xzcabbage
 * @since 2025/10/3
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupService groupService;
    private final ShortLinkActualRemoteService remoteService;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLinks(ShortLinkRecycleBinPageReqDTO req) {
        List<GroupDO> list = groupService.list(
                new LambdaQueryWrapper<GroupDO>().eq(GroupDO::getUsername, UserContext.getUsername()));
        if (list.isEmpty()) {
            throw new ServiceException(GROUP_NOT_EXISTS);
        }
        req.setGidList(list.stream().map(GroupDO::getGid).collect(Collectors.toList()));
        return remoteService.pageRecycleBinShortLinks(req.getGidList(), req.getCurrent(), req.getSize());
    }
}
