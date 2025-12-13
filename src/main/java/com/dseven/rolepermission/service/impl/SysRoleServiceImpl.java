package com.dseven.rolepermission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.entity.SysPermission;
import com.dseven.rolepermission.entity.SysRole;
import com.dseven.rolepermission.entity.SysRolePermission;
import com.dseven.rolepermission.mapper.SysRoleMapper;
import com.dseven.rolepermission.service.SysRolePermissionService;
import com.dseven.rolepermission.service.SysRoleService;
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

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限关联
        rolePermissionService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRolePermission>()
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
}