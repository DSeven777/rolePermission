package com.dseven.rolepermission.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dseven.rolepermission.common.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper接口
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 根据父部门ID查询子部门列�?
     * @param parentId 父部门ID
     * @return 子部门列�?
     */
    @Select("SELECT * FROM sys_dept WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort ASC")
    List<SysDept> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询部门及其子部门ID列表
     * @param deptId 部门ID
     * @return 部门ID列表
     */
    @Select("SELECT id FROM sys_dept WHERE id = #{deptId} OR parent_id = #{deptId} " +
            "OR parent_id IN (SELECT id FROM sys_dept WHERE parent_id = #{deptId}) " +
            "AND deleted = 0")
    List<Long> selectDeptAndChildrenIds(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询部门ID列表（数据权限）
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);
}

