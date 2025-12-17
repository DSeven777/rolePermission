package com.dseven.rolepermission.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联表
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    /**
     * 角色ID
     */
    @TableId
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}