-- 创建数据库
CREATE DATABASE IF NOT EXISTS seckill_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seckill_db;

-- 创建用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
                        username VARCHAR(50) NOT NULL COMMENT '用户名',
                        password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
                        phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
                        email VARCHAR(50) DEFAULT '' COMMENT '邮箱',
                        role INT NOT NULL DEFAULT 2 COMMENT '角色：1-管理员/2-普通用户',
                        create_time DATETIME NOT NULL COMMENT '创建时间'
) COMMENT '用户表';

-- 插入测试管理员用户（用户名：admin，密码：123456）
INSERT INTO t_user (username, password, phone, email, role, create_time)
VALUES ('admin', '123456', '13800138000', 'admin@seckill.com', 1, NOW());