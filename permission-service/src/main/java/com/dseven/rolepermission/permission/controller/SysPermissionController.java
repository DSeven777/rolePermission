package com.dseven.rolepermission.permission.controller;

import com.dseven.rolepermission.common.entity.SysPermission;
import com.dseven.rolepermission.common.result.Result;
import com.dseven.rolepermission.permission.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class SysPermissionController {
    private final SysPermissionService permissionService;

    @GetMapping("/role/{roleId}")
    public Result<List<SysPermission>> getPermissionsByRoleId(@PathVariable("roleId") Long roleId) {
        return Result.success(permissionService.selectByRoleId(roleId));
    }
}
