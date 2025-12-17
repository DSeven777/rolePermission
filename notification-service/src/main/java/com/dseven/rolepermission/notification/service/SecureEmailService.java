package com.dseven.rolepermission.notification.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.dseven.rolepermission.notification.config.EmailProperties;
import com.dseven.rolepermission.notification.entity.EmailLog;
import com.dseven.rolepermission.notification.enums.EmailBizType;
import com.dseven.rolepermission.common.exception.BizException;
import com.dseven.rolepermission.notification.mapper.EmailLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecureEmailService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MailSenderManager mailSenderManager;
    private final EmailProperties properties;
    private final EmailLogMapper emailLogMapper;

    /**
     * 发送验证码 (核心入口)
     * 无论邮箱是否存在，均返回成功，防止撞库
     */
    public void sendCode(String email, EmailBizType bizType, String clientIp) {
        // 1. IP 频控检查 (Redis 滑动窗口或计数器)
        checkIpLimit(clientIp);

        // 2. 邮箱频控检查
        String rateKey = String.format("email:rate:%s:%s", bizType.getType(), email);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
            throw new BizException("发送过于频繁，请稍后再试"); 
        }

        // 3. 生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 4. 异步发送邮件 (关键优化)
        mailSenderManager.sendAsync(email, bizType, code);

        // 5. 存入 Redis (事务或 Pipeline 保证原子性)
        String codeKey = String.format("email:code:%s:%s", bizType.getType(), email);
        String tryKey = String.format("email:try:%s:%s", bizType.getType(), email);
        
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                // 存验证码
                operations.opsForValue().set(codeKey, code, properties.getCodeExpireMinutes(), TimeUnit.MINUTES);
                // 清空旧的试错计数
                operations.delete(tryKey);
                // 设置发送间隔
                operations.opsForValue().set(rateKey, "1", properties.getSendIntervalSeconds(), TimeUnit.SECONDS);
                return operations.exec();
            }
        });
        
        // 6. 记录日志 (脱敏/Hash)
        logEmailAction(email, bizType, code, clientIp);
    }
    
    /**
     * 校验验证码并颁发 Token
     * @return 验证成功后的临时 Token
     */
    public String verifyCode(String email, EmailBizType bizType, String inputCode) {
        String codeKey = String.format("email:code:%s:%s", bizType.getType(), email);
        String tryKey = String.format("email:try:%s:%s", bizType.getType(), email);

        // 1. 检查是否存在
        String realCode = (String) redisTemplate.opsForValue().get(codeKey);
        if (realCode == null) {
            throw new BizException("验证码已过期或未发送");
        }

        // 2. 检查验证码是否匹配
        if (!realCode.equals(inputCode)) {
            // 累加错误次数
            Long retries = redisTemplate.opsForValue().increment(tryKey);
            if (retries != null && retries >= properties.getMaxTryCount()) {
                // 超过最大尝试次数，直接删除验证码，强制重新获取
                redisTemplate.delete(codeKey);
                redisTemplate.delete(tryKey);
                throw new BizException("验证码已失效(错误次数过多)");
            }
            throw new BizException("验证码错误，剩余重试次数: " + (properties.getMaxTryCount() - retries));
        }

        // 3. 验证成功
        // 删除验证码防止二次使用 (或者保留但标记为已使用，视业务需求)
        redisTemplate.delete(codeKey);
        redisTemplate.delete(tryKey);

        // 4. 生成一次性业务 Token (关键步骤)
        String verifyToken = UUID.randomUUID().toString();
        String tokenKey = "email:token:" + verifyToken;
        
        // Token 中存储 邮箱+业务类型，有效期 15 分钟
        String tokenValue = email + ":" + bizType.getType();
        redisTemplate.opsForValue().set(tokenKey, tokenValue, 15, TimeUnit.MINUTES);

        return verifyToken;
    }

    /**
     * 最终业务操作 (如重置密码)
     * 必须携带 verifyToken
     */
    public void consumeToken(String verifyToken, EmailBizType expectedType, Runnable businessAction) {
        String tokenKey = "email:token:" + verifyToken;
        String value = (String) redisTemplate.opsForValue().get(tokenKey);
        
        if (value == null) {
            throw new BizException("操作令牌无效或已过期，请重新验证");
        }
        
        // 解析数据
        String[] parts = value.split(":");
        // String email = parts[0]; // 如果业务需要email，可以传递出去
        String type = parts[1];
        
        if (!expectedType.getType().equals(type)) {
            throw new BizException("令牌类型不匹配");
        }
        
        // 执行业务逻辑
        businessAction.run();
        
        // 🚀 销毁 Token (防重放)
        redisTemplate.delete(tokenKey);
    }

    private void checkIpLimit(String ip) {
        String ipKey = "email:rate:ip:" + ip;
        Long count = redisTemplate.opsForValue().increment(ipKey);
        if (count != null && count == 1) {
            redisTemplate.expire(ipKey, 1, TimeUnit.HOURS);
        }
        if (count != null && count > properties.getMaxSendPerHourPerIp()) {
            throw new BizException("当前IP请求次数过多");
        }
    }

    private void logEmailAction(String email, EmailBizType type, String code, String ip) {
        String codeHash = SecureUtil.hmacSha256(properties.getSecretSalt()).digestHex(code);
        
        EmailLog logEntry = new EmailLog();
        logEntry.setEmail(email);
        logEntry.setBizType(type.getType());
        logEntry.setCodeHash(codeHash); // 仅存 Hash
        logEntry.setClientIp(ip);
        logEntry.setCreateTime(LocalDateTime.now());
        
        // 记录入库
        try {
            emailLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("Failed to log email action", e);
        }
    }
}
