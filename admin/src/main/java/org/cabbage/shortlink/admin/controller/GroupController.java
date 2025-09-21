package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.result.Result;
import org.cabbage.shortlink.admin.common.convention.result.Results;
import org.cabbage.shortlink.admin.dto.req.LinkGroupAddReqDTO;
import org.cabbage.shortlink.admin.dto.req.LinkGroupSortReqDTO;
import org.cabbage.shortlink.admin.dto.req.LinkGroupUpdateReqDTO;
import org.cabbage.shortlink.admin.dto.resp.LinkGroupRespDTO;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 删除分组名
     *
     * @param gid 分组gid
     * @return 成功
     */
    @RequestMapping(value = "/api/short-link/v1/group/delete", method = RequestMethod.POST)
    public Result<Void> deleteGroup(@RequestParam("gid") String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 排序分组
     *
     * @param req 排序请求
     * @return 成功
     */
    @RequestMapping(value = "/api/short-link/v1/group/sort", method = RequestMethod.POST)
    public Result<Void> sortGroup(@RequestBody List<LinkGroupSortReqDTO> req) {
        groupService.sortGroup(req);
        return Results.success();
    }
}
