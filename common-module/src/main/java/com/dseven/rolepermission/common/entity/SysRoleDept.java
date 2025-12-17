package com.dseven.rolepermission.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-部门关联表（自定义数据权限）
 */
@Data
@TableName("sys_role_dept")
public class SysRoleDept {

    /**
     * 角色ID
     */
    @TableId
    private Long roleId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}