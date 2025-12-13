package com.dseven.rolepermission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dseven.rolepermission.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色编码，如 admin/user
     */
    private String code;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 数据范围：1本人 2本部门 3本部门及子部门 4全部 5自定义
     */
    private Integer dataScope;

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;

    /**
     * 权限ID列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> permissionIds;

    /**
     * 部门ID列表（用于数据权限，非数据库字段）
     */
    @TableField(exist = false)
    private List<Long> deptIds;
}