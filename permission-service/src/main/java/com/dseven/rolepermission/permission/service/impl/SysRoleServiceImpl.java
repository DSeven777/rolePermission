package com.dseven.rolepermission.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.common.entity.SysRole;
import com.dseven.rolepermission.common.entity.SysRolePermission;
import com.dseven.rolepermission.common.entity.SysUserRole;
import com.dseven.rolepermission.permission.mapper.SysRoleMapper;
import com.dseven.rolepermission.permission.service.SysRolePermissionService;
import com.dseven.rolepermission.permission.service.SysRoleService;
import com.dseven.rolepermission.permission.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRolePermissionService rolePermissionService;
    private final SysUserRoleService userRoleService;

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        rolePermissionService.remove(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));

        // 添加新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> rolePermissions = permissionIds.stream()
                    .map(permissionId -> {
                        SysRolePermission rolePermission = new SysRolePermission();
                        rolePermission.setRoleId(roleId);
                        rolePermission.setPermissionId(permissionId);
                        rolePermission.setCreateTime(LocalDateTime.now());
                        return rolePermission;
                    })
                    .collect(Collectors.toList());

            return rolePermissionService.saveBatch(rolePermissions);
        }

        return true;
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return baseMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public boolean deleteUserRoles(Long userId) {
        return userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
    }

    @Override
    public boolean deleteUsersRoles(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        return userRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignUserRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        deleteUserRoles(userId);

        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = roleIds.stream()
                    .map(roleId -> {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(roleId);
                        userRole.setCreateTime(LocalDateTime.now());
                        return userRole;
                    })
                    .collect(Collectors.toList());

            return userRoleService.saveBatch(userRoles);
        }

        return true;
    }
}
