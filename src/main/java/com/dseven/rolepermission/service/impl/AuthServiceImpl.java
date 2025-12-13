package com.dseven.rolepermission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dseven.rolepermission.entity.SysPermission;
import com.dseven.rolepermission.entity.SysRole;
import com.dseven.rolepermission.entity.SysUser;
import com.dseven.rolepermission.sso.dto.LoginRequest;
import com.dseven.rolepermission.sso.dto.LoginResponse;
import com.dseven.rolepermission.sso.dto.RegisterRequest;
import com.dseven.rolepermission.service.*;
import com.dseven.rolepermission.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysPermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final String EMAIL_CODE_KEY_PREFIX = "email_code:";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final long CAPTCHA_EXPIRE_TIME = 5; // 验证码5分钟过期
    private static final long EMAIL_CODE_EXPIRE_TIME = 10; // 邮箱验证码10分钟过期

    @Override
    public LoginResponse login(LoginRequest loginRequest, String clientIp) {
        // 1. 验证验证码（如果开启验证码功能）
        // validateCaptcha(loginRequest.getCaptchaKey(), loginRequest.getCaptcha());

        // 2. 查询用户
        SysUser user = userService.getByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        log.info("当前登陆人：{}",user);
        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 5. 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("ip", clientIp);

        String accessToken = jwtUtil.generateToken(user.getUsername(), claims);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // 6. 更新最后登录信息
        updateLastLoginInfo(user, clientIp);

        // 7. 构建响应
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn(jwtUtil.getTokenRemainingTime(accessToken));
        response.setUserInfo(buildUserInfo(user));

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest registerRequest, String clientIp) {
        // 1. 验证两次密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 2. 验证邮箱验证码
        validateEmailCode(registerRequest.getEmail(), registerRequest.getEmailCode());

        // 3. 检查用户名是否已存在
        if (userService.getByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 4. 检查邮箱是否已注册
        if (userService.lambdaQuery()
                .eq(SysUser::getEmail, registerRequest.getEmail())
                .exists()) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 5. 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname());
        user.setEmail(registerRequest.getEmail());
        user.setDeptId(registerRequest.getDeptId());
        user.setStatus(1);

        if (!userService.createUser(user)) {
            throw new RuntimeException("注册失败");
        }

        // 6. 删除已使用的邮箱验证码
        String emailCodeKey = EMAIL_CODE_KEY_PREFIX + registerRequest.getEmail();
        redisTemplate.delete(emailCodeKey);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        // 将token加入黑名单
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + accessToken;
            long expireTime = jwtUtil.getTokenRemainingTime(accessToken);
            redisTemplate.opsForValue().set(blacklistKey, "1", expireTime, TimeUnit.SECONDS);
        }

        // 将刷新token加入黑名单
        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + refreshToken;
            long expireTime = jwtUtil.getTokenRemainingTime(refreshToken);
            redisTemplate.opsForValue().set(blacklistKey, "1", expireTime, TimeUnit.SECONDS);
        }
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        // 1. 验证刷新token
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        // 2. 检查token是否在黑名单中
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + refreshToken;
        if (redisTemplate.hasKey(blacklistKey)) {
            throw new RuntimeException("刷新令牌已失效");
        }

        // 3. 获取用户名
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 4. 验证用户是否存在且状态正常
        SysUser user = userService.getByUsername(username);
        if (user == null || user.getStatus() == 0) {
            throw new RuntimeException("用户不存在或已被禁用");
        }

        // 5. 生成新的token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());

        String newAccessToken = jwtUtil.generateToken(user.getUsername(), claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        // 6. 将旧的刷新token加入黑名单
        redisTemplate.opsForValue().set(blacklistKey, "1",
                jwtUtil.getTokenRemainingTime(refreshToken), TimeUnit.SECONDS);

        // 7. 返回新的token
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", newAccessToken);
        tokenMap.put("refreshToken", newRefreshToken);
        tokenMap.put("tokenType", "Bearer");
        tokenMap.put("expiresIn", String.valueOf(jwtUtil.getTokenRemainingTime(newAccessToken)));

        return tokenMap;
    }

    @Override
    public Map<String, String> generateCaptcha() {
        // TODO: 实现验证码生成逻辑
        // 可以使用 Google kaptcha 或其他验证码库
        String captchaKey = UUID.randomUUID().toString();
        String captchaCode = "1234"; // 临时使用固定值，实际应该生成随机验证码

        // 存储验证码到Redis
        redisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + captchaKey, captchaCode,
                CAPTCHA_EXPIRE_TIME, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", "data:image/png;base64,"); // TODO: 生成验证码图片的base64编码

        return result;
    }

    @Override
    public void sendEmailCode(String email) {
        // 1. 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 2. 存储验证码到Redis
        String key = EMAIL_CODE_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, EMAIL_CODE_EXPIRE_TIME, TimeUnit.MINUTES);

        // 3. 发送邮件
        // TODO: 实现邮件发送逻辑
        log.info("发送邮箱验证码：{} -> {}", email, code);
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        // 1. 验证邮箱验证码
        validateEmailCode(email, code);

        // 2. 查找用户
        SysUser user = userService.lambdaQuery()
                .eq(SysUser::getEmail, email)
                .one();

        if (user == null) {
            throw new RuntimeException("邮箱未注册");
        }

        // 3. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        if (!userService.updateById(user)) {
            throw new RuntimeException("密码重置失败");
        }

        // 4. 删除已使用的验证码
        String emailCodeKey = EMAIL_CODE_KEY_PREFIX + email;
        redisTemplate.delete(emailCodeKey);
    }

    @Override
    public LoginResponse.UserInfo getUserInfo(String token) {
        // 1. 验证token
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("无效的令牌");
        }

        // 2. 检查token是否在黑名单中
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
        if (redisTemplate.hasKey(blacklistKey)) {
            throw new RuntimeException("令牌已失效");
        }

        // 3. 获取用户信息
        String username = jwtUtil.getUsernameFromToken(token);
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        return buildUserInfo(user);
    }

    /**
     * 构建用户信息
     */
    private LoginResponse.UserInfo buildUserInfo(SysUser user) {
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setEmail(user.getEmail());
        userInfo.setDeptId(user.getDeptId());
        // userInfo.setDeptName(); // TODO: 查询部门名称
        userInfo.setLastLoginTime(user.getUpdateTime());
        // userInfo.setLastLoginIp(); // TODO: 从登录记录中获取

        // 获取用户角色
        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        List<String> roleNames = roles.stream()
                .map(SysRole::getCode)
                .collect(Collectors.toList());
        userInfo.setRoles(roleNames);

        // 获取用户权限
        Set<String> permissions = new HashSet<>();
        for (SysRole role : roles) {
            List<SysPermission> permissionList = permissionService.selectByRoleId(role.getId());
            List<String> permissionCodes = permissionList.stream()
                    .map(SysPermission::getCode)
                    .collect(Collectors.toList());
            permissions.addAll(permissionCodes);
        }
        userInfo.setPermissions(new ArrayList<>(permissions));

        return userInfo;
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(SysUser user, String clientIp) {
        // TODO: 可以更新用户的最后登录时间和IP
        // 也可以记录登录日志
        log.info("用户 {} 登录，IP：{}", user.getUsername(), clientIp);
    }

    /**
     * 验证验证码
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        if (captchaKey != null && captcha != null) {
            String key = CAPTCHA_KEY_PREFIX + captchaKey;
            String storedCaptcha = (String) redisTemplate.opsForValue().get(key);

            if (storedCaptcha == null) {
                throw new RuntimeException("验证码已过期");
            }

            if (!storedCaptcha.equalsIgnoreCase(captcha)) {
                throw new RuntimeException("验证码错误");
            }

            // 验证成功后删除验证码
            redisTemplate.delete(key);
        }
    }

    /**
     * 验证邮箱验证码
     */
    private void validateEmailCode(String email, String code) {
        String key = EMAIL_CODE_KEY_PREFIX + email;
        String storedCode = (String) redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            throw new RuntimeException("邮箱验证码已过期");
        }

        if (!storedCode.equals(code)) {
            throw new RuntimeException("邮箱验证码错误");
        }
    }
}