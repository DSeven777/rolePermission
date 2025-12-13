package com.dseven.rolepermission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dseven.rolepermission.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据父权限ID查询子权限列表
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY id ASC")
    List<SysPermission> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<SysPermission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0")
    List<SysPermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限类型查询权限列表
     * @param type 权限类型：1菜单 2按钮 3接口
     * @return 权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE type = #{type} AND deleted = 0 ORDER BY id ASC")
    List<SysPermission> selectByType(@Param("type") Integer type);
}