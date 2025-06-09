package org.cabbage.shortlink.admin.service.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;
import org.cabbage.shortlink.admin.dao.entity.User;
import org.cabbage.shortlink.admin.dto.resp.UserRespDTO;

public interface UserService extends IService<User> {

    UserRespDTO getUserByUsername(String username);
}
