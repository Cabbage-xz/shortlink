package org.cabbage.shortlink.admin.service.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.admin.dao.entity.GroupDO;

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
}
