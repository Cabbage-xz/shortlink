package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.result.Result;
import org.cabbage.shortlink.admin.common.convention.result.Results;
import org.cabbage.shortlink.admin.dto.req.LinkGroupAddReqDTO;
import org.cabbage.shortlink.admin.dto.req.LinkGroupUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.LinkGroupRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xzcabbage
 * @since 2025/9/21
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增分组
     *
     * @param req 分组添加请求
     * @return 成功
     */
    @RequestMapping(value = "/api/short-link/v1/group/save", method = RequestMethod.POST)
    public Result<Void> save(@RequestBody LinkGroupAddReqDTO req) {
        groupService.saveGroup(req.getName());
        return Results.success();
    }

    /**
     * 查询所有分组
     *
     * @return 所有分组
     */
    @RequestMapping(value = "/api/short-link/v1/group/query", method = RequestMethod.GET)
    public Result<List<LinkGroupRespDTO>> queryGroups() {
        return Results.success(groupService.queryGroup());
    }

    /**
     * 修改分组名
     *
     * @param req 分组修改请求
     * @return 成功
     */
    @RequestMapping(value = "/api/short-link/v1/group/update", method = RequestMethod.POST)
    public Result<Void> updateGroupName(@RequestBody LinkGroupUpdateReqDTO req) {
        groupService.updateGroupName(req);
        return Results.success();
    }
}
