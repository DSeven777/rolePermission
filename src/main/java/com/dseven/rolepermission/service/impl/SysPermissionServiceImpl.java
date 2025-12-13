package com.dseven.rolepermission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.entity.SysPermission;
import com.dseven.rolepermission.mapper.SysPermissionMapper;
import com.dseven.rolepermission.service.SysPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务实现类
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public List<SysPermission> selectByRoleId(Long roleId) {
        return baseMapper.selectByRoleId(roleId);
    }
}