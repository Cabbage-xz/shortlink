package org.cabbage.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.cabbage.shortlink.admin.common.biz.user.UserContext;
import org.cabbage.shortlink.admin.dao.entity.GroupDO;
import org.cabbage.shortlink.admin.dao.mapper.GroupMapper;
import org.cabbage.shortlink.admin.dto.req.LinkGroupUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.LinkGroupRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.cabbage.shortlink.admin.toolkit.RandomGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接分组接口层实现类
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {


    /**
     * 新增分组
     *
     * @param groupName 分组名称
     */
    @Override
    public void saveGroup(String groupName) {
        String gid;
        // 生成唯一gid
        do {
            gid = RandomGenerator.generateRandom();
        } while (isHasGid(gid));
        GroupDO group = GroupDO.builder()
                .gid(gid)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .name(groupName)
                .build();
        save(group);
    }

    /**
     * 查询所有分组
     *
     * @return 所有分组
     */
    @Override
    public List<LinkGroupRespDTO> queryGroup() {
        // todo 获取用户名
        List<GroupDO> list = list(new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(List.of(GroupDO::getSortOrder, GroupDO::getUpdateTime)));
        return BeanUtil.copyToList(list, LinkGroupRespDTO.class);

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
     * 查询gid是否已存在
     *
     * @param gid gid
     * @return true->gid已存在 false表示gid未存在
     */
    private boolean isHasGid(String gid) {
        List<GroupDO> list = list(new LambdaQueryWrapper<GroupDO>()
                .eq(GroupDO::getGid, gid)
                // todo 设置用户名
                .eq(GroupDO::getUsername, UserContext.getUsername()));
        return list != null && !list.isEmpty();
    }
}
