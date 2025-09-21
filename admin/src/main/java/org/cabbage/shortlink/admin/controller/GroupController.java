package org.cabbage.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.cabbage.shortlink.admin.service.interfaces.GroupService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xzcabbage
 * @since 2025/9/21
 */
@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
}
