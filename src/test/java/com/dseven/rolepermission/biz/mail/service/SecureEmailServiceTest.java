package com.dseven.rolepermission.biz.mail.service;

import com.dseven.rolepermission.biz.mail.config.EmailProperties;
import com.dseven.rolepermission.biz.mail.entity.EmailLog;
import com.dseven.rolepermission.biz.mail.enums.EmailBizType;
import com.dseven.rolepermission.biz.mail.exception.BizException;
import com.dseven.rolepermission.biz.mail.mapper.EmailLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 邮箱验证服务单元测试
 *
 * 测试覆盖：
 * 1. 发送验证码功能
 * 2. 验证码校验功能
 * 3. Token 消费功能
 * 4. 错误场景和边界条件
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("邮箱验证服务测试")
class SecureEmailServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MailSenderManager mailSenderManager;

    @Mock
    private EmailProperties properties;

    @Mock
    private EmailLogMapper emailLogMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SecureEmailService secureEmailService;

    private String testEmail;
    private String testIp;
    private String testCode;

    @BeforeEach
    void setUp() {
        testEmail = "test@qq.com";
        testIp = "127.0.0.1";
        testCode = "123456";

        // 配置默认属性
        when(properties.getCodeExpireMinutes()).thenReturn(10);
        when(properties.getSendIntervalSeconds()).thenReturn(60);
        when(properties.getMaxTryCount()).thenReturn(3);
        when(properties.getMaxSendPerHourPerIp()).thenReturn(10);
        when(properties.getSecretSalt()).thenReturn("test_salt");
        when(properties.getFromAddress()).thenReturn("test@dseven77.online");

        // 配置 Redis 模板
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            RedisOperations<String, Object> operations = mock(RedisOperations.class);
            when(operations.opsForValue()).thenReturn(valueOperations);
            doNothing().when(operations).multi();
            when(operations.delete(anyString())).thenReturn(true);
            when(operations.exec()).thenReturn(new ArrayList<>());
            return callback.execute(operations);
        });
    }

    @Test
    @DisplayName("发送验证码 - 成功场景")
    void testSendCode_Success() {
        // Given
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        assertDoesNotThrow(() -> secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp));

        // Then
        // 验证邮件发送被调用
        verify(mailSenderManager).sendAsync(eq(testEmail), eq(EmailBizType.REGISTER), anyString());

        // 验证 Redis 事务执行
        verify(redisTemplate).execute(any(SessionCallback.class));

        // 验证日志记录
        verify(emailLogMapper).insert(any(EmailLog.class));
    }

    @Test
    @DisplayName("发送验证码 - IP 频控限制")
    void testSendCode_IpRateLimit() {
        // Given
        when(valueOperations.increment(anyString())).thenReturn(11L); // 超过限制

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp));
        assertEquals("当前IP请求次数过多", exception.getMessage());

        // 验证不会发送邮件
        verify(mailSenderManager, never()).sendAsync(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("发送验证码 - 邮箱频控限制")
    void testSendCode_EmailRateLimit() {
        // Given
        when(redisTemplate.hasKey(anyString())).thenReturn(true); // 发送间隔未过
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp));
        assertEquals("发送过于频繁，请稍后再试", exception.getMessage());

        // 验证不会发送邮件
        verify(mailSenderManager, never()).sendAsync(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("发送验证码 - 数据库异常不影响主流程")
    void testSendCode_DatabaseError() {
        // Given
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        // 使用具体的 EmailLog 类型来消除歧义
        doThrow(new RuntimeException("Database error")).when(emailLogMapper).insert(any(EmailLog.class));

        // When & Then - 主流程不应该失败
        assertDoesNotThrow(() -> secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp));

        // 验证邮件仍然发送
        verify(mailSenderManager).sendAsync(eq(testEmail), eq(EmailBizType.REGISTER), anyString());
    }

    @Test
    @DisplayName("验证验证码 - 成功场景")
    void testVerifyCode_Success() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(testCode);

        // When
        String token = secureEmailService.verifyCode(testEmail, EmailBizType.REGISTER, testCode);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("-")); // UUID 格式

        // 验证 token 被存储
        verify(valueOperations).set(contains("email:token:"),
                eq(testEmail + ":" + EmailBizType.REGISTER.getType()),
                eq(15L),
                eq(TimeUnit.MINUTES));

        // 验证验证码被删除
        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    @DisplayName("验证验证码 - 验证码不存在")
    void testVerifyCode_CodeNotExists() {
        // Given
        when(valueOperations.get(anyString())).thenReturn(null);

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.verifyCode(testEmail, EmailBizType.REGISTER, testCode));
        assertEquals("验证码已过期或未发送", exception.getMessage());
    }

    @Test
    @DisplayName("验证验证码 - 验证码错误")
    void testVerifyCode_WrongCode() {
        // Given
        when(valueOperations.get(anyString())).thenReturn("654321"); // 错误的验证码
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.verifyCode(testEmail, EmailBizType.REGISTER, testCode));
        assertTrue(exception.getMessage().contains("验证码错误"));
        assertTrue(exception.getMessage().contains("剩余重试次数: 2"));
    }

    @Test
    @DisplayName("验证验证码 - 超过最大重试次数")
    void testVerifyCode_ExceedMaxRetries() {
        // Given
        when(valueOperations.get(anyString())).thenReturn("wrongcode");
        when(valueOperations.increment(anyString())).thenReturn(3L); // 第3次错误

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.verifyCode(testEmail, EmailBizType.REGISTER, testCode));
        assertEquals("验证码已失效(错误次数过多)", exception.getMessage());

        // 验证验证码被删除
        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    @DisplayName("消费 Token - 成功场景")
    void testConsumeToken_Success() {
        // Given
        String token = "test-token";
        String tokenValue = testEmail + ":" + EmailBizType.RESET_PASSWORD.getType();
        when(valueOperations.get("email:token:" + token)).thenReturn(tokenValue);

        // Mock 业务操作
        Runnable mockAction = mock(Runnable.class);

        // When
        assertDoesNotThrow(() -> secureEmailService.consumeToken(token, EmailBizType.RESET_PASSWORD, mockAction));

        // Then
        // 验证业务操作被执行
        verify(mockAction).run();

        // 验证 token 被删除
        verify(redisTemplate).delete("email:token:" + token);
    }

    @Test
    @DisplayName("消费 Token - Token 无效")
    void testConsumeToken_InvalidToken() {
        // Given
        String token = "invalid-token";
        when(valueOperations.get("email:token:" + token)).thenReturn(null);
        Runnable mockAction = mock(Runnable.class);

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.consumeToken(token, EmailBizType.RESET_PASSWORD, mockAction));
        assertEquals("操作令牌无效或已过期，请重新验证", exception.getMessage());

        // 验证业务操作未执行
        verify(mockAction, never()).run();
    }

    @Test
    @DisplayName("消费 Token - Token 类型不匹配")
    void testConsumeToken_TokenTypeMismatch() {
        // Given
        String token = "test-token";
        String tokenValue = testEmail + ":" + EmailBizType.REGISTER.getType(); // 错误的类型
        when(valueOperations.get("email:token:" + token)).thenReturn(tokenValue);
        Runnable mockAction = mock(Runnable.class);

        // When & Then
        BizException exception = assertThrows(BizException.class,
                () -> secureEmailService.consumeToken(token, EmailBizType.RESET_PASSWORD, mockAction));
        assertEquals("令牌类型不匹配", exception.getMessage());

        // 验证业务操作未执行
        verify(mockAction, never()).run();
    }

    @Test
    @DisplayName("测试不同业务类型")
    void testDifferentBizTypes() {
        // Test each business type
        EmailBizType[] types = {EmailBizType.REGISTER, EmailBizType.RESET_PASSWORD, EmailBizType.BIND_EMAIL};

        for (EmailBizType type : types) {
            when(redisTemplate.hasKey(anyString())).thenReturn(false);
            when(valueOperations.increment(anyString())).thenReturn(1L);

            assertDoesNotThrow(() -> secureEmailService.sendCode(testEmail, type, testIp));

            verify(mailSenderManager).sendAsync(eq(testEmail), eq(type), anyString());

            // Reset mocks for next iteration
            reset(mailSenderManager, redisTemplate);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }
    }

    @Test
    @DisplayName("测试 Redis Pipeline 操作")
    void testRedisPipelineOperations() {
        // Given
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        secureEmailService.sendCode(testEmail, EmailBizType.REGISTER, testIp);

        // Then
        // 验证使用了 Redis 事务（SessionCallback）
        verify(redisTemplate).execute(any(SessionCallback.class));

        // 验证事务中的操作
        ArgumentCaptor<SessionCallback> callbackCaptor = ArgumentCaptor.forClass(SessionCallback.class);
        verify(redisTemplate).execute(callbackCaptor.capture());
    }
}