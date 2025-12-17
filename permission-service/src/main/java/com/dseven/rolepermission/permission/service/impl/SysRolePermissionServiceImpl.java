package com.dseven.rolepermission.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.common.entity.SysRolePermission;
import com.dseven.rolepermission.permission.mapper.SysRolePermissionMapper;
import com.dseven.rolepermission.permission.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色-权限关联服务实现�?
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {
}

