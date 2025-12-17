package com.dseven.rolepermission.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dseven.rolepermission.common.entity.SysUser;
import com.dseven.rolepermission.common.feign.RemotePermissionService;
import com.dseven.rolepermission.common.result.Result;
import com.dseven.rolepermission.user.mapper.SysUserMapper;
import com.dseven.rolepermission.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final RemotePermissionService permissionService;

    @Override
    public SysUser getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return baseMapper.selectByUsername(username);
    }

    @Override
    public Page<SysUser> selectUserPage(Page<SysUser> page, String username, Integer status, Long deptId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询用户名
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }

        // 状态过滤
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        // 部门过滤
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }

        // 排序
        wrapper.orderByDesc(SysUser::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        // 检查用户名是否已存在
        if (getByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        // 检查用户是否存在
        SysUser existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果修改了用户名，检查是否重复
        if (!existUser.getUsername().equals(user.getUsername())) {
            if (getByUsername(user.getUsername()) != null) {
                throw new RuntimeException("用户名已存在");
            }
        }

        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        // 删除用户角色关联
        permissionService.deleteUserRoles(userId);

        return this.removeById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        // 批量删除用户角色关联
        permissionService.deleteUsersRoles(userIds);

        return this.removeByIds(userIds);
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        return this.updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId, String password) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(password);
        return this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        Result<Boolean> result = permissionService.assignUserRoles(userId, roleIds);
        return Boolean.TRUE.equals(result.getData());
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }
}
