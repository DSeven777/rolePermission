package com.dseven.rolepermission.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dseven.rolepermission.common.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色-权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 删除角色的所有权�?
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除权限的所有角�?
     * @param permissionId 权限ID
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    void deleteByPermissionId(@Param("permissionId") Long permissionId);
}

