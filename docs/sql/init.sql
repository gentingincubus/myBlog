-- ============================================================
-- 博客系统初始化 SQL 脚本
-- 包含：数据库创建、用户表 (sys_user)、导航表 (site_nav)
-- ============================================================

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `myblog`;

-- 1. 用户表 (sys_user)
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加盐哈希）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT '' COMMENT '头像链接',
  `email` VARCHAR(100) DEFAULT '' COMMENT '邮箱',
  `status` INT DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 预置默认管理员账号 (账号: admin , 密码: password 或者 123456)
-- 采用标准的 BCrypt 密文
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`) VALUES
(1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '管理员', 1)
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 2. 站点顶部导航菜单表 (site_nav)
CREATE TABLE IF NOT EXISTS `site_nav` (
  `id` BIGINT NOT NULL COMMENT '导航菜单ID (雪花算法 19 位)',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称 (如: 首页、个人介绍、花活工坊)',
  `path` VARCHAR(100) NOT NULL COMMENT '路由跳转路径 (如: /、/about、/tools)',
  `icon` VARCHAR(50) DEFAULT '' COMMENT '图标或Emoji (如: 🏠、👤、🧪、🛠️)',
  `sort` INT DEFAULT 0 COMMENT '排序权重 (数字越小越靠前)',
  `is_blank` TINYINT NOT NULL DEFAULT 0 COMMENT '是否新窗口打开 (0: 否, 1: 是)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  INDEX `idx_sort` (`sort`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点顶部导航菜单表';

INSERT INTO `site_nav` (`id`, `name`, `path`, `icon`, `sort`, `is_blank`, `deleted`) VALUES
(1001, '首页', '/', '🏠', 1, 0, 0),
(1002, '空间概览', '/overview', '🏞️', 2, 0, 0),
(1003, '漫游地图', '/map', '🗺️', 3, 0, 0),
(1004, '站点导航', '/explore', '🧭', 4, 0, 0)
ON DUPLICATE KEY UPDATE `id`=`id`;
