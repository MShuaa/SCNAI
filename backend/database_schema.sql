-- SCNAI植鉴系统 - 精简版数据库表结构(仅3张表)
-- 数据库创建
CREATE DATABASE IF NOT EXISTS scnai_plant DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scnai_plant;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE `users` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（明文）',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `role` VARCHAR(20) DEFAULT '普通用户' COMMENT '角色：系统管理员/普通用户',
  `organization` VARCHAR(100) DEFAULT NULL COMMENT '所属组织',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `login_count` INT(11) DEFAULT 0 COMMENT '登录次数',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试用户
-- 用户名: admin
-- 密码: password123（明文存储）
INSERT INTO `users` (`username`, `password`, `email`, `real_name`, `role`) VALUES
('admin', 'password123', 'admin@scnai.com', '系统管理员', '系统管理员');

-- ========================================
-- 2. 植物信息表
-- ========================================
CREATE TABLE `plants` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '植物ID',
  `name` VARCHAR(100) NOT NULL COMMENT '植物名称',
  `scientific_name` VARCHAR(150) DEFAULT NULL COMMENT '学名',
  `family` VARCHAR(100) DEFAULT NULL COMMENT '科',
  `genus` VARCHAR(100) DEFAULT NULL COMMENT '属',
  `description` TEXT DEFAULT NULL COMMENT '描述',
  `growth_habit` TEXT DEFAULT NULL COMMENT '生长习性',
  `planting_method` TEXT DEFAULT NULL COMMENT '种植方法',
  `common_diseases` TEXT DEFAULT NULL COMMENT '常见病害（JSON格式）',
  `prevention_tips` TEXT DEFAULT NULL COMMENT '预防措施',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物信息表';

-- 插入测试植物数据
INSERT INTO `plants` (`name`, `scientific_name`, `family`, `genus`, `description`, `growth_habit`, `planting_method`, `common_diseases`, `prevention_tips`) VALUES
('丝瓜', 'Luffa cylindrica', '葫芦科', '丝瓜属',
 '丝瓜是葫芦科一年生攀援藤本，原产于印度，在中国广泛栽培。果实长圆柱形，嫩果可食用，成熟后的网状纤维可作洗涤用具。',
 '喜温暖湿润气候，不耐寒，生长适温20-30℃。对土壤要求不严，但以排水良好、富含有机质的壤土为佳。',
 '播种前浸种催芽，直播或育苗移栽均可。株距40-50cm，行距1.5-2m。需搭架引蔓，及时整枝打杈。',
 '[{"name":"白粉病","code":"powdery"},{"name":"霜霉病","code":"downy"},{"name":"炭疽病","code":"anthracnose"},{"name":"病毒病","code":"virus"},{"name":"蚜虫","code":"aphid"}]',
 '1. 选用抗病品种；2. 合理密植，保证通风透光；3. 及时清除病残体；4. 科学施肥，增强植株抗性；5. 定期喷施预防性药剂。');

-- ========================================
-- 3. 识别记录表
-- ========================================
CREATE TABLE `recognition_records` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` INT(11) NOT NULL COMMENT '用户ID',
  `plant_id` INT(11) DEFAULT NULL COMMENT '植物ID',
  `plant_name` VARCHAR(100) DEFAULT '丝瓜' COMMENT '植物名称',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图像URL',
  `thumbnail_url` VARCHAR(255) DEFAULT NULL COMMENT '缩略图URL',
  `disease_type` VARCHAR(50) NOT NULL COMMENT '病害类型代码：powdery/downy/anthracnose/virus/aphid/healthy',
  `disease_type_name` VARCHAR(50) NOT NULL COMMENT '病害名称：白粉病/霜霉病等',
  `confidence` DECIMAL(5,4) NOT NULL COMMENT '置信度（0-1）',
  `severity` VARCHAR(20) NOT NULL COMMENT '严重程度：轻度/中度/重度',
  `area` VARCHAR(50) DEFAULT NULL COMMENT '区域',
  `location_lat` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `location_lng` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `status` VARCHAR(20) DEFAULT '未处理' COMMENT '处理状态：未处理/处理中/已处理',
  `symptoms` TEXT DEFAULT NULL COMMENT '症状描述',
  `treatment_plan` TEXT DEFAULT NULL COMMENT '处理方案',
  `identify_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '识别时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `identify_time`),
  KEY `idx_plant_id` (`plant_id`),
  KEY `idx_disease_time` (`disease_type`, `identify_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病虫害识别记录表';

-- ========================================
-- 说明
-- ========================================
-- 精简版数据库，仅保留核心3张表：
-- 1. users: 用户认证和个人信息
-- 2. plants: 植物信息库
-- 3. recognition_records: 图像识别记录
--
-- 测试账号:
-- 用户名: admin
-- 密码: password123
