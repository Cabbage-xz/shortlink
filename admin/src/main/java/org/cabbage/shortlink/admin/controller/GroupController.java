package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.common.convention.result.Result;
import org.cabbage.shortlink.admin.common.convention.result.Results;
import org.cabbage.shortlink.admin.dto.req.LinkGroupReqDTO;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/9/21
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @RequestMapping(value = "/api/short-link/v1/group/save", method = RequestMethod.POST)
    public Result<Void> save(@RequestBody LinkGroupReqDTO req) {
        groupService.saveGroup(req.getName());
        return Results.success();
    }
}
