package com.dseven.rolepermission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dseven.rolepermission.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /**
     * 权限唯一标识，如 order:delete
     */
    private String code;

    /**
     * 权限名称，如 删除订单
     */
    private String name;

    /**
     * 类型：1菜单 2按钮 3接口/API
     */
    private Integer type;

    /**
     * 上级权限，用于菜单结构
     */
    private Long parentId;

    /**
     * 菜单/接口路径
     */
    private String path;

    /**
     * 图标（非数据库字段，前端使用）
     */
    @TableField(exist = false)
    private String icon;

    /**
     * 组件路径（非数据库字段，前端使用）
     */
    @TableField(exist = false)
    private String component;

    /**
     * 是否外链（非数据库字段，前端使用）
     */
    @TableField(exist = false)
    private Boolean isExternal;

    /**
     * 是否隐藏（非数据库字段，前端使用）
     */
    @TableField(exist = false)
    private Boolean hidden;

    /**
     * 子权限列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<SysPermission> children;
}