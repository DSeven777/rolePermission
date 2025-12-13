package com.dseven.rolepermission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dseven.rolepermission.entity.SysPermission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectByRoleId(Long roleId);
}