package org.cabbage.shortlink.admin.service.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.admin.dao.entity.GroupDO;
import org.cabbage.shortlink.admin.dto.resp.LinkGroupRespDTO;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/9/21
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     *
     * @param groupName 分组名称
     */
    void saveGroup(String groupName);

    /**
     * 查询所有分组
     * @return 所有分组
     */
    List<LinkGroupRespDTO> queryGroup();
}
