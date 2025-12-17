package com.dseven.rolepermission.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（秒�?)
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "用户�?)
        private String username;

        @Schema(description = "昵称")
        private String nickname;

        @Schema(description = "邮箱")
        private String email;

        @Schema(description = "头像URL")
        private String avatar;

        @Schema(description = "部门ID")
        private Long deptId;

        @Schema(description = "部门名称")
        private String deptName;

        @Schema(description = "角色列表")
        private List<String> roles;

        @Schema(description = "权限列表")
        private List<String> permissions;

        @Schema(description = "最后登录时�?)
        private LocalDateTime lastLoginTime;

        @Schema(description = "最后登录IP")
        private String lastLoginIp;
    }
}

