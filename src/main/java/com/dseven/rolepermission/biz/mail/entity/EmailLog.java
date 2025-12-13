package com.dseven.rolepermission.biz.mail.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_email_log")
public class EmailLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String bizType;
    
    /**
     * 验证码指纹(HMAC-SHA256)
     */
    private String codeHash;
    
    private Integer sendStatus; // 0: Success, 1: Fail
    private String clientIp;
    private String errorMsg;
    private LocalDateTime createTime;
}
