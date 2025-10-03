package org.cabbage.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.common.convention.result.Result;
import org.cabbage.shortlink.common.convention.result.Results;
import org.cabbage.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import org.cabbage.shortlink.project.service.RecycleBinService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/10/3
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;


    @RequestMapping(value = "/api/short-link/v1/recycle-bin/save", method = RequestMethod.POST)
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO req) {
        recycleBinService.saveRecycleBin(req);
        return Results.success();
    }
}
