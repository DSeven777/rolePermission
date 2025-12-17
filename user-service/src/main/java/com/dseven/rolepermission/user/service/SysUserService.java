package com.dseven.rolepermission.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dseven.rolepermission.common.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用�?
     * @param username 用户�?
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 分页查询用户列表
     * @param page 分页参数
     * @param username 用户名（模糊查询�?
     * @param status 状�?
     * @param deptId 部门ID
     * @return 用户分页列表
     */
    Page<SysUser> selectUserPage(Page<SysUser> page, String username, Integer status, Long deptId);

    /**
     * 创建用户
     * @param user 用户信息
     * @return 是否成功
     */
    boolean createUser(SysUser user);

    /**
     * 更新用户
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 批量删除用户
     * @param userIds 用户ID列表
     * @return 是否成功
     */
    boolean deleteUsers(List<Long> userIds);

    /**
     * 更新用户状�?
     * @param userId 用户ID
     * @param status 状�?
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param password 新密�?
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String password);

    /**
     * 分配用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色ID列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> getUserRoleIds(Long userId);
}

