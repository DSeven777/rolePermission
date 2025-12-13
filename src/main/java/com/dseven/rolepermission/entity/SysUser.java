package com.dseven.rolepermission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dseven.rolepermission.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 登录用户名
     */
    private String username;

    private String email;
    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;

    /**
     * 所属部门名称（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private String deptName;
}