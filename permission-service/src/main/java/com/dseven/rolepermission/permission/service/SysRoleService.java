package com.dseven.rolepermission.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dseven.rolepermission.common.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID获取角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    // New methods for User-Role management
    boolean deleteUserRoles(Long userId);
    boolean deleteUsersRoles(List<Long> userIds);
    boolean assignUserRoles(Long userId, List<Long> roleIds);
}
