package com.dseven.rolepermission.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dseven.rolepermission.common.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用�?
     * @param username 用户�?
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据部门ID查询用户列表
     * @param deptId 部门ID
     * @return 用户列表
     */
    @Select("SELECT * FROM sys_user WHERE dept_id = #{deptId} AND deleted = 0")
    List<SysUser> selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据角色ID查询用户列表
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Select("SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0")
    List<SysUser> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
}

