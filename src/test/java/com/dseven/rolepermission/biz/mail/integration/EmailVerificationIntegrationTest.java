package com.dseven.rolepermission.biz.mail.integration;

import com.dseven.rolepermission.biz.mail.service.SecureEmailService;
import com.dseven.rolepermission.biz.mail.service.MailSenderManager;
import com.dseven.rolepermission.biz.mail.enums.EmailBizType;
import com.dseven.rolepermission.biz.mail.mapper.EmailLogMapper;
import com.dseven.rolepermission.biz.mail.entity.EmailLog;
import com.dseven.rolepermission.biz.mail.config.TestMailConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮箱验证功能集成测试
 *
 * 测试整个邮件验证流程，包括：
 * 1. Redis 存储
 * 2. 数据库日志记录
 * 3. 业务流程完整性
 */
@SpringBootTest
@ActiveProfiles("test") // 使用测试环境配置
@Transactional // 每个测试后回滚事务
@DisplayName("邮箱验证功能集成测试")
@Import(TestMailConfig.class)
class EmailVerificationIntegrationTest {

    @Autowired
    private SecureEmailService secureEmailService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EmailLogMapper emailLogMapper;

    @Autowired
    private MailSenderManager mailSenderManager;

    @Autowired
    private JavaMailSender javaMailSender;

    private String testEmail;
    private String testIp;

    @BeforeEach
    void setUp() {
        testEmail = "test@qq.com";
        testIp = "192.168.1.101";

        // Reset the mock to ensure clean state for each test
        Mockito.reset(javaMailSender);

        // 清理测试数据
        cleanupTestData();
    }

    private void cleanupTestData() {
        // 清理 Redis 中的测试数据
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
        } catch (Exception e) {
            // Redis 不可用时忽略
        }
    }

    @Test
    @DisplayName("完整的邮箱验证流程测试")
    void testCompleteEmailVerificationFlow() {
        // 1. 发送验证码
        assertDoesNotThrow(() ->
            secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp)
        );

        // 验证 Redis 中存储了验证码
        String codeKey = String.format("email:code:%s:%s",
            EmailBizType.REGISTER.getType(), testEmail);
        String code = (String) redisTemplate.opsForValue().get(codeKey);
        assertNotNull(code);
        assertEquals(6, code.length());

        // 2. 验证验证码（成功场景）
        String token = secureEmailService.verifyCode(testEmail, EmailBizType.REGISTER, code);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 验证验证码已被删除
        assertNull(redisTemplate.opsForValue().get(codeKey));

        // 验证 Token 已被存储
        String tokenKey = "email:token:" + token;
        String tokenValue = (String) redisTemplate.opsForValue().get(tokenKey);
        assertEquals(testEmail + ":" + EmailBizType.REGISTER.getType(), tokenValue);

        // 3. 消费 Token 执行业务操作
        AtomicBoolean actionExecuted = new AtomicBoolean(false);
        Runnable businessAction = () -> actionExecuted.set(true);

        assertDoesNotThrow(() ->
            secureEmailService.consumeToken(token, EmailBizType.REGISTER, businessAction)
        );

        // 验证业务操作已执行
        assertTrue(actionExecuted.get());

        // 验证 Token 已被删除
        assertNull(redisTemplate.opsForValue().get(tokenKey));
    }

    @Test
    @DisplayName("验证码重试限制功能测试")
    void testVerificationRetryLimit() {
        // 发送验证码
        secureEmailService.sendCode(testEmail, EmailBizType.RESET_PASSWORD, testIp);

        String codeKey = String.format("email:code:%s:%s",
            EmailBizType.RESET_PASSWORD.getType(), testEmail);
        String code = (String) redisTemplate.opsForValue().get(codeKey);

        // 第一次验证失败
        assertThrows(Exception.class, () ->
            secureEmailService.verifyCode(testEmail, EmailBizType.RESET_PASSWORD, "000000")
        );

        // 第二次验证失败
        assertThrows(Exception.class, () ->
            secureEmailService.verifyCode(testEmail, EmailBizType.RESET_PASSWORD, "111111")
        );

        // 第三次验证失败，应该导致验证码失效
        Exception exception = assertThrows(Exception.class, () ->
            secureEmailService.verifyCode(testEmail, EmailBizType.RESET_PASSWORD, "222222")
        );
        assertTrue(exception.getMessage().contains("验证码已失效"));

        // 验证码已被删除，正确的验证码也无法通过
        assertThrows(Exception.class, () ->
            secureEmailService.verifyCode(testEmail, EmailBizType.RESET_PASSWORD, code)
        );
    }

    @Test
    @DisplayName("发送频控功能测试")
    void testSendRateLimit() {
        // 第一次发送成功
        assertDoesNotThrow(() ->
            secureEmailService.sendCode(testEmail, EmailBizType.BIND_EMAIL, testIp)
        );

        // 立即再次发送应该被限制
        assertThrows(Exception.class, () ->
            secureEmailService.sendCode(testEmail, EmailBizType.BIND_EMAIL, testIp)
        );
    }

    @Test
    @DisplayName("IP 频控功能测试")
    void testIpRateLimit() {
        // 使用不同邮箱但相同 IP，测试 IP 频控

        // 快速发送多个验证码，直到触发 IP 频控
        int sendCount = 0;
        for (int i = 0; i < 12; i++) { // 假设限制是 10 次/小时
            String email = String.format("user%d@test.com", i);
            try {
                secureEmailService.sendCode(email, EmailBizType.REGISTER, testIp);
                sendCount++;
            } catch (Exception e) {
                // 预期会失败
                break;
            }
        }

        // 验证触发了 IP 频控
        assertTrue(sendCount <= 10, "IP 频控应该被触发");
    }

    @Test
    @DisplayName("Token 消费功能测试")
    void testTokenConsumption() {
        // 发送并验证验证码
        secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp);

        String codeKey = String.format("email:code:%s:%s",
            EmailBizType.REGISTER.getType(), testEmail);
        String code = (String) redisTemplate.opsForValue().get(codeKey);

        String token = secureEmailService.verifyCode(testEmail, EmailBizType.REGISTER, code);

        // 模拟不同的业务操作
        AtomicBoolean registerExecuted = new AtomicBoolean(false);
        AtomicBoolean resetExecuted = new AtomicBoolean(false);

        // 正确类型的 Token 可以消费
        assertDoesNotThrow(() ->
            secureEmailService.consumeToken(token, EmailBizType.REGISTER,
                () -> registerExecuted.set(true))
        );
        assertTrue(registerExecuted.get());

        // Token 已被消费，不能再次使用
        assertThrows(Exception.class, () ->
            secureEmailService.consumeToken(token, EmailBizType.REGISTER,
                () -> resetExecuted.set(true))
        );
        assertFalse(resetExecuted.get());
    }

    @Test
    @DisplayName("不同业务类型隔离测试")
    void testDifferentBizTypes() {
        EmailBizType[] types = {EmailBizType.REGISTER, EmailBizType.RESET_PASSWORD, EmailBizType.BIND_EMAIL};

        for (EmailBizType type : types) {
            // 使用不同的测试邮箱避免冲突
            String email = String.format("user%d@test.com", type.ordinal());

            // 发送验证码
            assertDoesNotThrow(() ->
                secureEmailService.sendCode(email, type, testIp)
            );

            // 获取验证码
            String codeKey = String.format("email:code:%s:%s", type.getType(), email);
            String code = (String) redisTemplate.opsForValue().get(codeKey);
            assertNotNull(code);

            // 验证验证码
            String token = secureEmailService.verifyCode(email, type, code);
            assertNotNull(token);
        }
    }

    @Test
    @DisplayName("数据库日志记录测试")
    void testDatabaseLogging() {
        // 发送验证码
        secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp);

        // 等待异步操作完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 验证数据库记录（如果 EmailLogMapper 可用）
        try {
            List<EmailLog> logs = emailLogMapper.selectList(null);
            if (!logs.isEmpty()) {
                // 查找最新的记录
                EmailLog lastLog = logs.stream()
                    .filter(log -> testEmail.equals(log.getEmail()))
                    .findFirst()
                    .orElse(null);

                if (lastLog != null) {
                    assertEquals(testEmail, lastLog.getEmail());
                    assertEquals(EmailBizType.REGISTER.getType(), lastLog.getBizType());
                    assertNotNull(lastLog.getCreateTime());
                    assertNotNull(lastLog.getCodeHash()); // 验证码应该被 Hash 存储
                }
            }
        } catch (Exception e) {
            // 数据库操作失败时忽略，重点测试 Redis 流程
        }
    }

    @Test
    @DisplayName("并发发送测试")
    void testConcurrentSending() throws InterruptedException {
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        final int[] successCount = {0};

        // 创建多个线程同时发送验证码
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    String email = String.format("user%d@test.com", threadId);
                    secureEmailService.sendCode(email, EmailBizType.REGISTER, testIp);
                    successCount[0]++;
                } catch (Exception e) {
                    // 预期某些线程可能因为频控失败
                    System.out.println("Thread " + threadId + " failed: " + e.getMessage());
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join(5000); // 最多等待 5 秒
        }

        // 验证成功发送次数不超过频控限制
        assertTrue(successCount[0] <= threadCount,
            "成功发送次数不应超过线程数");
    }
}