package org.cabbage.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.admin.common.biz.user.UserContext;
import org.cabbage.shortlink.admin.dao.entity.GroupDO;
import org.cabbage.shortlink.admin.dao.mapper.GroupMapper;
import org.cabbage.shortlink.admin.dto.req.LinkGroupSortReqDTO;
import org.cabbage.shortlink.admin.dto.req.LinkGroupUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.LinkGroupRespDTO;
import org.cabbage.shortlink.admin.remote.ShortLinkRemoteService;
import org.cabbage.shortlink.common.convention.exception.ClientException;
import org.cabbage.shortlink.common.dto.resp.ShortLinkCountQueryRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.cabbage.shortlink.admin.toolkit.RandomGenerator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.cabbage.shortlink.admin.common.enums.GroupErrorCodeEnum.GROUP_NUM_TOO_MANY;
import static org.cabbage.shortlink.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接分组接口层实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final RedissonClient redissonClient;

    @Value("${short-link.group.max-num}")
    private Integer maxNum;

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {

    };

    /**
     * 新增分组
     *
     * @param groupName 分组名称
     */
    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            long count = count(new LambdaQueryWrapper<GroupDO>().eq(GroupDO::getUsername, username));
            if (count >= maxNum) {
                throw new ClientException(GROUP_NUM_TOO_MANY);
            }
            String gid;
            // 生成唯一gid
            do {
                gid = RandomGenerator.generateRandom();
            } while (isHasGid(username, gid));
            GroupDO group = GroupDO.builder()
                    .gid(gid)
                    .username(username)
                    .sortOrder(0)
                    .name(groupName)
                    .build();
            save(group);
        } finally {
            lock.unlock();
        }

    }

    /**
     * 查询所有分组
     *
     * @return 所有分组
     */
    @Override
    public List<LinkGroupRespDTO> queryGroup() {
        List<GroupDO> list = list(new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(List.of(GroupDO::getSortOrder, GroupDO::getUpdateTime)));
        Map<String, Integer> gidCountMap = shortLinkRemoteService.listShortLinkCount(list.stream().map(GroupDO::getGid)
                        .collect(Collectors.toList())).getData().stream()
                .collect(Collectors.toMap(ShortLinkCountQueryRespDTO::getGid, ShortLinkCountQueryRespDTO::getShortLinkCount));
        return list.stream().map(group -> {
            LinkGroupRespDTO linkGroupRespDTO = new LinkGroupRespDTO();
            BeanUtil.copyProperties(group, linkGroupRespDTO);
            linkGroupRespDTO.setShortLinkCount(gidCountMap.getOrDefault(group.getGid(), 0));
            return linkGroupRespDTO;
        }).collect(Collectors.toList());

    }

    /**
     * 修改分组名
     *
     * @param req 修改请求参数
     */
    @Override
    public void updateGroupName(LinkGroupUpdateReqDTO req) {
        GroupDO group = GroupDO.builder().name(req.getName()).build();
        update(group, new LambdaUpdateWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, req.getGid()));
    }

    /**
     * 删除分组
     *
     * @param gid 分组标识
     */
    @Override
    public void deleteGroup(String gid) {
        GroupDO group = new GroupDO();
        group.setDelFlag(1);
        update(group, new LambdaUpdateWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid));
    }

    /**
     * 排序分组
     *
     * @param req 排序请求
     */
    @Override
    @Transactional
    public void sortGroup(List<LinkGroupSortReqDTO> req) {

        Map<String, Integer> gidToSortOrder = req.stream()
                .collect(Collectors.toMap(LinkGroupSortReqDTO::getGid, LinkGroupSortReqDTO::getSortOrder,
                        // 合并函数 只保留相同key出现的第一个value
                        (existing, replacement) -> existing));
        List<GroupDO> groupDOList = list(new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .in(GroupDO::getGid, gidToSortOrder.keySet()));
        groupDOList.forEach(groupDO -> groupDO.setSortOrder(gidToSortOrder.get(groupDO.getGid())));
        updateBatchById(groupDOList);
    }

    /**
     * 查询gid是否已存在
     *
     * @param username 用户名
     * @param gid gid
     * @return true->gid已存在 false表示gid未存在
     */
    private boolean isHasGid(String username, String gid) {
        List<GroupDO> list = list(new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername())));
        return list != null && !list.isEmpty();
    }
}
