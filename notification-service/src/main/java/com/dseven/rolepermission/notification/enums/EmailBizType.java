package com.dseven.rolepermission.notification.enums;

public enum EmailBizType {
    REGISTER("register", "用户注册"),
    RESET_PASSWORD("reset_password", "重置密码"),
    BIND_EMAIL("bind_email", "绑定邮箱");

    private final String type;
    private final String desc;

    EmailBizType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}


