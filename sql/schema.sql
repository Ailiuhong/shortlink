CREATE DATABASE IF NOT EXISTS shortlink
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE shortlink;

CREATE TABLE IF NOT EXISTS short_link
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    short_code VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NULL COMMENT 'Base62 短码，创建时由自增 ID 生成',
    long_url   VARCHAR(2048) NOT NULL COMMENT '原始长链接',
    created_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_short_code (short_code),
    KEY idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '短链映射表';

CREATE TABLE IF NOT EXISTS access_log
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    short_code VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    client_ip  VARCHAR(64)  NOT NULL DEFAULT '',
    user_agent VARCHAR(512) NOT NULL DEFAULT '',
    referer    VARCHAR(1024) NOT NULL DEFAULT '',
    visited_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_code_visited_at (short_code, visited_at),
    KEY idx_visited_at (visited_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '短链访问日志表';
