package com.dseven.rolepermission.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dseven.rolepermission.common.entity.SysRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = 1")
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的权限关�?
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissions(@Param("roleId") Long roleId);

    /**
     * 删除角色的部门关联（数据权限�?
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_dept WHERE role_id = #{roleId}")
    void deleteRoleDepts(@Param("roleId") Long roleId);

    /**
     * 查询角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);
}

