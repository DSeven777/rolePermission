package com.dseven.rolepermission.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求DTO
 */
@Data
@Schema(description = "注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为�?)
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之�?)
    @Schema(description = "用户�?, example = "testuser")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须�?-20个字符之�?)
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456")
    private String confirmPassword;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字�?)
    @Schema(description = "昵称", example = "测试用户")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正�?)
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    @Schema(description = "手机�?, example = "13800138000")
    private String phone;

    @Schema(description = "部门ID", example = "1")
    private Long deptId;

    @NotBlank(message = "验证码不能为�?)
    @Schema(description = "邮箱验证�?, example = "123456")
    private String emailCode;

    @Schema(description = "邀请码", example = "INVITE123")
    private String inviteCode;
}

