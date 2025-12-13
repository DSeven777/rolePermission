CREATE TABLE sys_user (
                          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                          username      VARCHAR(64) NOT NULL UNIQUE COMMENT '登录用户名',
                          password      VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                          nickname      VARCHAR(64) COMMENT '昵称',
                          dept_id       BIGINT DEFAULT NULL COMMENT '所属部门',

                          status        TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',

                          create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          INDEX idx_dept_id (dept_id),
                          INDEX idx_status (status)
) COMMENT='用户表';
CREATE TABLE sys_dept (
                          id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                          parent_id   BIGINT DEFAULT 0 COMMENT '父部门ID',
                          name        VARCHAR(64) NOT NULL COMMENT '部门名称',
                          sort        INT DEFAULT 0,

                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          INDEX idx_parent (parent_id)
) COMMENT='部门表';
CREATE TABLE sys_role (
                          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                          code          VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码，如 admin/user',
                          name          VARCHAR(64) NOT NULL COMMENT '角色名称',

                          data_scope    TINYINT DEFAULT 1 COMMENT '数据范围：1本人 2本部门 3本部门及子部门 4全部 5自定义',

                          status        TINYINT DEFAULT 1 COMMENT '1正常 0禁用',

                          create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          INDEX idx_status (status)
) COMMENT='角色表';
CREATE TABLE sys_permission (
                                id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                                code        VARCHAR(128) NOT NULL UNIQUE COMMENT '权限唯一标识，如 order:delete',
                                name        VARCHAR(128) NOT NULL COMMENT '权限名称，如 删除订单',
                                type        TINYINT NOT NULL COMMENT '1菜单 2按钮 3接口/API',

                                parent_id   BIGINT DEFAULT 0 COMMENT '上级权限，用于菜单结构',
                                path        VARCHAR(255) DEFAULT NULL COMMENT '菜单/接口路径',

                                create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                INDEX idx_type (type),
                                INDEX idx_parent (parent_id)
) COMMENT='权限点表（菜单/按钮/API）';
CREATE TABLE sys_user_role (
                               user_id   BIGINT NOT NULL,
                               role_id   BIGINT NOT NULL,
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY (user_id, role_id),

                               INDEX idx_role_id (role_id),
                               CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
                               CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
) COMMENT='用户-角色关联表';
CREATE TABLE sys_role_permission (
                                     role_id       BIGINT NOT NULL,
                                     permission_id BIGINT NOT NULL,
                                     create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     PRIMARY KEY (role_id, permission_id),

                                     INDEX idx_perm_id (permission_id),
                                     CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
                                     CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) COMMENT='角色-权限关联表';
CREATE TABLE sys_role_dept (
                               role_id     BIGINT NOT NULL,
                               dept_id     BIGINT NOT NULL,
                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               PRIMARY KEY(role_id, dept_id),

                               INDEX idx_dept_id (dept_id)
) COMMENT='角色-部门（自定义数据权限）';

ALTER TABLE sys_user
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER status;

CREATE INDEX idx_user_deleted ON sys_user(deleted);
ALTER TABLE sys_dept
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER sort;

CREATE INDEX idx_dept_deleted ON sys_dept(deleted);
ALTER TABLE sys_role
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER status;

CREATE INDEX idx_role_deleted ON sys_role(deleted);
ALTER TABLE sys_permission
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER path;

CREATE INDEX idx_permission_deleted ON sys_permission(deleted);
ALTER TABLE sys_user_role
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER create_time;

CREATE INDEX idx_user_role_deleted ON sys_user_role(deleted);
ALTER TABLE sys_role_permission
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER create_time;

CREATE INDEX idx_role_permission_deleted ON sys_role_permission(deleted);
ALTER TABLE sys_role_dept
    ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除'
AFTER create_time;

CREATE INDEX idx_role_dept_deleted ON sys_role_dept(deleted);
