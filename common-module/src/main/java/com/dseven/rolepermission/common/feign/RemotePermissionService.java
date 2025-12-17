package com.dseven.rolepermission.common.feign;

import com.dseven.rolepermission.common.entity.SysPermission;
import com.dseven.rolepermission.common.entity.SysRole;
import com.dseven.rolepermission.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "permission-service", contextId = "remotePermissionService")
public interface RemotePermissionService {

    @GetMapping("/role/user/{userId}")
    Result<List<SysRole>> getRolesByUserId(@PathVariable("userId") Long userId);

    @GetMapping("/permission/role/{roleId}")
    Result<List<SysPermission>> getPermissionsByRoleId(@PathVariable("roleId") Long roleId);

    @DeleteMapping("/role/user/{userId}")
    Result<Boolean> deleteUserRoles(@PathVariable("userId") Long userId);

    @DeleteMapping("/role/users")
    Result<Boolean> deleteUsersRoles(@RequestParam("userIds") List<Long> userIds);

    @PostMapping("/role/user/{userId}/assign")
    Result<Boolean> assignUserRoles(@PathVariable("userId") Long userId, @RequestBody List<Long> roleIds);
}
