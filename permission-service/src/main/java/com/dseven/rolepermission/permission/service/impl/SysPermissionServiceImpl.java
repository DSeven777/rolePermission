package com.dseven.rolepermission.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.common.entity.SysPermission;
import com.dseven.rolepermission.permission.mapper.SysPermissionMapper;
import com.dseven.rolepermission.permission.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务实现�?
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public List<SysPermission> selectByRoleId(Long roleId) {
        return baseMapper.selectByRoleId(roleId);
    }
}

