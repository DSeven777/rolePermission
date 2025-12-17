package com.dseven.rolepermission.auth.service;

import com.dseven.rolepermission.sso.dto.LoginRequest;
import com.dseven.rolepermission.sso.dto.LoginResponse;
import com.dseven.rolepermission.sso.dto.RegisterRequest;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest, String clientIp);

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @param clientIp 客户端IP
     */
    void register(RegisterRequest registerRequest, String clientIp);

    /**
     * 用户登出
     * @param accessToken 访问令牌
     * @param refreshToken 刷新令牌
     */
    void logout(String accessToken, String refreshToken);

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的令牌信息
     */
    Map<String, String> refreshToken(String refreshToken);

    /**
     * 生成验证�?
     * @return 验证码信�?
     */
    Map<String, String> generateCaptcha();

    /**
     * 发送邮箱验证码
     * @param email 邮箱地址
     */
    void sendEmailCode(String email);

    /**
     * 重置密码
     * @param email 邮箱
     * @param code 验证�?
     * @param newPassword 新密�?
     */
    void resetPassword(String email, String code, String newPassword);

    /**
     * 获取用户信息
     * @param token 令牌
     * @return 用户信息
     */
    LoginResponse.UserInfo getUserInfo(String token);
}

