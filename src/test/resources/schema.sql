CREATE TABLE IF NOT EXISTS sys_email_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    biz_type VARCHAR(50) NOT NULL,
    code_hash VARCHAR(64),
    send_status TINYINT DEFAULT 0,
    client_ip VARCHAR(50),
    error_msg VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
