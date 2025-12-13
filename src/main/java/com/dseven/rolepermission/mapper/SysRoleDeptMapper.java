package com.dseven.rolepermission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dseven.rolepermission.entity.SysRoleDept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色-部门关联Mapper接口（数据权限）
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    /**
     * 删除角色的所有部门权限
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_dept WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除部门的所有角色
     * @param deptId 部门ID
     */
    @Delete("DELETE FROM sys_role_dept WHERE dept_id = #{deptId}")
    void deleteByDeptId(@Param("deptId") Long deptId);
}