# BizException 业务异常类说明

## 概述

`BizException` 是一个自定义的业务异常类，用于封装和处理应用程序中的业务逻辑错误。

## 类定义

```java
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 设计特点

### 1. 继承自 RuntimeException
- **非受检异常**：不需要在方法签名中声明 `throws`
- **简化调用**：调用方无需强制捕获或声明
- **业务导向**：通常由上层框架统一捕获和处理

### 2. 两个构造函数

#### `BizException(String message)`
- **用途**：创建简单的业务异常
- **参数**：错误信息字符串
- **示例**：`throw new BizException("验证码已过期");`

#### `BizException(String message, Throwable cause)`
- **用途**：创建带有原因异常的业务异常
- **参数**：
  - `message`：业务错误信息
  - `cause`：底层的异常原因
- **示例**：`throw new BizException("邮件发送失败", e);`

## 使用场景

### 1. 业务规则校验
```java
if (user == null) {
    throw new BizException("用户不存在");
}
```

### 2. 权限校验
```java
if (!hasPermission(user, resource)) {
    throw new BizException("没有访问权限");
}
```

### 3. 业务流程控制
```java
// 在邮箱验证方案中的应用
if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey))) {
    throw new BizException("发送过于频繁，请稍后再试");
}
```

### 4. 异常包装
```java
try {
    mailSender.send(email, content);
} catch (MailException e) {
    // 包装底层异常，提供更友好的错误信息
    throw new BizException("邮件发送服务异常", e);
}
```

## 优势

1. **统一异常类型**：所有业务异常都使用同一类型，便于统一处理
2. **保留异常链**：通过 `cause` 参数可以追踪原始异常
3. **简化调用代码**：无需层层传递异常
4. **更好的错误信息**：可以提供面向用户的友好错误信息

## 最佳实践

1. **错误信息要明确**：提供清晰、具体的错误原因
2. **合理使用**：只用于业务逻辑错误，不要用于系统级错误
3. **配合全局异常处理器**：通过 `@ControllerAdvice` 统一处理
4. **避免过度使用**：不是所有错误都需要抛异常

## 与其他异常类型的区别

| 异常类型 | 特点 | 使用场景 |
|---------|------|----------|
| **BizException** | 业务逻辑错误 | 用户输入错误、业务规则违反 |
| **SystemException** | 系统级错误 | 数据库连接失败、网络超时 |
| **Checked Exception** | 受检异常 | 必须显式处理的异常 |
| **RuntimeException** | 运行时异常 | 编程错误、不可预见的错误 |

## 在项目中的扩展建议

```java
public class BizException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    // 扩展：支持错误码
    public BizException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    // 扩展：支持国际化参数
    public BizException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    // getter 方法...
}
```

这样的扩展可以支持：
- 错误码标准化
- 国际化错误信息
- 更细粒度的异常分类