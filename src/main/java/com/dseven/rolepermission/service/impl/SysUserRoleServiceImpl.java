package com.dseven.rolepermission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.entity.SysUserRole;
import com.dseven.rolepermission.mapper.SysUserRoleMapper;
import com.dseven.rolepermission.service.SysUserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户-角色关联服务实现类
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}