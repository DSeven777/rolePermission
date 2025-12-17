package com.dseven.rolepermission.permission.controller;

import com.dseven.rolepermission.common.entity.SysRole;
import com.dseven.rolepermission.common.result.Result;
import com.dseven.rolepermission.permission.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {
    private final SysRoleService roleService;

    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getRolesByUserId(@PathVariable("userId") Long userId) {
        return Result.success(roleService.getRolesByUserId(userId));
    }

    @DeleteMapping("/user/{userId}")
    public Result<Boolean> deleteUserRoles(@PathVariable("userId") Long userId) {
        return Result.success(roleService.deleteUserRoles(userId));
    }

    @DeleteMapping("/users")
    public Result<Boolean> deleteUsersRoles(@RequestParam("userIds") List<Long> userIds) {
        return Result.success(roleService.deleteUsersRoles(userIds));
    }

    @PostMapping("/user/{userId}/assign")
    public Result<Boolean> assignUserRoles(@PathVariable("userId") Long userId, @RequestBody List<Long> roleIds) {
        return Result.success(roleService.assignUserRoles(userId, roleIds));
    }
}
