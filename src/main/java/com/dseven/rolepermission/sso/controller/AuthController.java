package com.dseven.rolepermission.sso.controller;

import com.dseven.rolepermission.common.result.Result;
import com.dseven.rolepermission.entity.SysUser;
import com.dseven.rolepermission.sso.dto.LoginRequest;
import com.dseven.rolepermission.sso.dto.LoginResponse;
import com.dseven.rolepermission.sso.dto.RegisterRequest;
import com.dseven.rolepermission.service.AuthService;
import com.dseven.rolepermission.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器 - 处理登录、注册、登出等认证相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "登录、注册、令牌管理")
public class AuthController {

    private final AuthService authService;
    private final SysUserService userService;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        log.info("用户登录: {}", loginRequest.getUsername());

        // 获取客户端IP
        String clientIp = getClientIpAddress(request);

        // 执行登录
        LoginResponse loginResponse = authService.login(loginRequest, clientIp);

        return Result.success("登录成功", loginResponse);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<String> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {

        log.info("用户注册: {}", registerRequest.getUsername());

        // 获取客户端IP
        String clientIp = getClientIpAddress(request);

        // 执行注册
        authService.register(registerRequest, clientIp);

        return Result.success("注册成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录")
    public Result<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "刷新令牌") @RequestParam(required = false) String refreshToken) {

        // 清除token
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token, refreshToken);
        }

        return Result.success("退出成功");
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<Map<String, String>> refreshToken(
            @Parameter(description = "刷新令牌", required = true)
            @NotBlank(message = "刷新令牌不能为空")
            @RequestParam String refreshToken) {

        Map<String, String> tokenMap = authService.refreshToken(refreshToken);

        return Result.success("刷新成功", tokenMap);
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取登录验证码")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captchaMap = authService.generateCaptcha();
        return Result.success(captchaMap);
    }

    @PostMapping("/send-email-code")
    @Operation(summary = "发送邮箱验证码", description = "注册时发送邮箱验证码")
    public Result<String> sendEmailCode(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @NotBlank(message = "邮箱不能为空") String email) {

        authService.sendEmailCode(email);
        return Result.success("验证码已发送");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过邮箱重置密码")
    public Result<String> resetPassword(
            @Parameter(description = "邮箱", required = true) @RequestParam String email,
            @Parameter(description = "验证码", required = true) @RequestParam String code,
            @Parameter(description = "新密码", required = true) @RequestParam String newPassword) {

        authService.resetPassword(email, code, newPassword);
        return Result.success("密码重置成功");
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名", description = "检查用户名是否可用")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名", required = true)
            @RequestParam @NotBlank(message = "用户名不能为空") String username) {

        boolean exists = userService.getByUsername(username) != null;
        return Result.success("检查完成", !exists);
    }

    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱", description = "检查邮箱是否已注册")
    public Result<Boolean> checkEmail(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @NotBlank(message = "邮箱不能为空") String email) {

        boolean exists = userService.lambdaQuery()
                .eq(SysUser::getEmail, email)
                .exists();
        return Result.success("检查完成", !exists);
    }

    @GetMapping("/user-info")
    @Operation(summary = "获取用户信息", description = "根据token获取当前登录用户信息")
    public Result<LoginResponse.UserInfo> getUserInfo(
            @RequestHeader(value = "Authorization", required = true) String authorization) {

        String token = authorization.substring(7);
        LoginResponse.UserInfo userInfo = authService.getUserInfo(token);

        return Result.success("获取成功", userInfo);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}