package com.dseven.rolepermission.sso.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @Schema(description = "验证码key", example = "captcha_key_123")
    private String captchaKey;

    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;
}