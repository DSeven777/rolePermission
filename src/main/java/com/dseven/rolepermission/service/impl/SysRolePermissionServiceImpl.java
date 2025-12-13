package com.dseven.rolepermission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.entity.SysRolePermission;
import com.dseven.rolepermission.mapper.SysRolePermissionMapper;
import com.dseven.rolepermission.service.SysRolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色-权限关联服务实现类
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements SysRolePermissionService {
}