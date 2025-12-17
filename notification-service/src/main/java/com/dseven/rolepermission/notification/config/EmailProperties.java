package com.dseven.rolepermission.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    /**
     * 验证码过期时间（分钟�?
     */
    private int codeExpireMinutes = 10;

    /**
     * 发送间隔限制（秒）
     */
    private int sendIntervalSeconds = 60;

    /**
     * 每小时最大发送次数（IP�?
     */
    private int maxSendPerHourPerIp = 10;

    /**
     * 每天最大发送次数（邮箱�?
     */
    private int maxSendPerDayPerEmail = 5;

    /**
     * 验证码最大尝试次�?
     */
    private int maxTryCount = 3;

    /**
     * 发件人名�?
     */
    private String fromName = "Role Permission System";

    /**
     * 发件人地址
     */
    private String fromAddress;

    /**
     * 签名Salt
     */
    private String secretSalt = "sys_secret_salt_default";
}


