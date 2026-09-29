-- ============================================================
-- MyBlog 全量数据库初始化与部署脚本 (生产与本地通用)
-- 包含以下 8 张核心数据表：
--   1. sys_user       - 系统用户表
--   2. sys_role       - RBAC 角色表
--   3. sys_user_role  - 用户与角色关联表
--   4. sys_menu       - 菜单路由与权限规则表
--   5. sys_role_menu  - 角色与菜单权限关联表
--   6. site_nav       - 站点顶部导航表
--   7. vr_category    - VR 园区分类表
--   8. vr_scene       - VR 全景场景点位表
-- ============================================================

CREATE DATABASE IF NOT EXISTS `myblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `myblog`;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- 1. 系统用户表 (sys_user)
-- ============================================================
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

-- 预置初始管理员账号 (用户名: admin, 初始密码: 123456)
-- 密码哈希值通过 BCrypt 加密生成
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`) VALUES
(1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '系统管理员', 1)
ON DUPLICATE KEY UPDATE `username`=VALUES(`username`);

-- ============================================================
-- 2. 系统角色表 (sys_role)
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称 (如: 超级管理员、普通用户)',
  `role_key` VARCHAR(50) NOT NULL COMMENT '角色权限字符 (如: admin、common)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态 (1: 正常, 0: 停用)',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '备注说明',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 预置角色：超级管理员 (admin)、普通用户 (common)
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `sort`, `status`, `remark`) VALUES
(1, '超级管理员', 'admin', 1, 1, '拥有系统全部功能与按钮最高权限'),
(2, '普通用户', 'common', 2, 1, '新注册用户默认角色，仅可查看仪表盘')
ON DUPLICATE KEY UPDATE `role_name`=VALUES(`role_name`);

-- ============================================================
-- 3. 用户与角色关联表 (sys_user_role)
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- 默认将 admin (ID: 1) 关联超级管理员 (ID: 1)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1)
ON DUPLICATE KEY UPDATE `user_id`=VALUES(`user_id`);

-- ============================================================
-- 4. 菜单与权限规则表 (sys_menu)
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT NOT NULL COMMENT '菜单/权限ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID (顶级为0)',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单或按钮名称',
  `menu_type` CHAR(1) NOT NULL COMMENT '菜单类型 (M: 目录, C: 菜单, F: 按钮操作)',
  `path` VARCHAR(200) DEFAULT '' COMMENT '路由地址 (仅目录/菜单有效)',
  `component` VARCHAR(255) DEFAULT '' COMMENT 'Vue组件路径 (如 admin/vr/VrSceneManageView)',
  `perms` VARCHAR(100) DEFAULT '' COMMENT '权限字符 (如 vr:scene:add)',
  `icon` VARCHAR(100) DEFAULT '' COMMENT '菜单图标 (如 Odometer, PictureFilled)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见 (1: 显示, 0: 隐藏)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单状态 (1: 正常, 0: 停用)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单与权限表';

-- 预置菜单树与按钮权限
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
-- 100: 仪表盘 (所有角色可见)
(100, 0, '仪表盘', 'C', '/admin/dashboard', 'admin/DashboardView', 'admin:dashboard:view', 'Odometer', 1),
-- 200: VR 全景中台 (目录)
(200, 0, 'VR 全景中台', 'M', '/admin/vr', '', '', 'View', 2),
(201, 200, '园区分类管理', 'C', '/admin/vr/category', 'admin/vr/VrCategoryManageView', 'vr:category:list', 'FolderOpened', 1),
(2011, 201, '新增分类', 'F', '', '', 'vr:category:add', '', 1),
(2012, 201, '修改分类', 'F', '', '', 'vr:category:edit', '', 2),
(2013, 201, '删除分类', 'F', '', '', 'vr:category:delete', '', 3),
(202, 200, '全景场景管理', 'C', '/admin/vr/scene', 'admin/vr/VrSceneManageView', 'vr:scene:list', 'PictureFilled', 2),
(2021, 202, '新增场景', 'F', '', '', 'vr:scene:add', '', 1),
(2022, 202, '修改场景', 'F', '', '', 'vr:scene:edit', '', 2),
(2023, 202, '删除场景', 'F', '', '', 'vr:scene:delete', '', 3),
(203, 200, '可视化打点', 'C', '/admin/vr/editor', 'admin/vr/VrMapEditorView', 'vr:editor:view', 'LocationInformation', 3),
(2031, 203, '保存打点', 'F', '', '', 'vr:editor:save', '', 1),
-- 300: 导航管理
(300, 0, '导航管理', 'C', '/admin/nav', 'admin/NavManageView', 'site:nav:list', 'Compass', 3),
(3001, 300, '新增导航', 'F', '', '', 'site:nav:add', '', 1),
(3002, 300, '修改导航', 'F', '', '', 'site:nav:edit', '', 2),
(3003, 300, '删除导航', 'F', '', '', 'site:nav:delete', '', 3),
-- 400: 系统管理 (目录)
(400, 0, '系统管理', 'M', '/admin/system', '', '', 'Setting', 4),
(401, 400, '用户管理', 'C', '/admin/system/user', 'admin/system/SysUserManageView', 'sys:user:list', 'User', 1),
(4011, 401, '用户查询', 'F', '', '', 'sys:user:query', '', 1),
(4012, 401, '分配角色', 'F', '', '', 'sys:user:role', '', 2),
(4013, 401, '状态切换', 'F', '', '', 'sys:user:status', '', 3),
(4014, 401, '重置密码', 'F', '', '', 'sys:user:resetPwd', '', 4),
(402, 400, '角色管理', 'C', '/admin/system/role', 'admin/system/SysRoleManageView', 'sys:role:list', 'Stamp', 2),
(4021, 402, '新增角色', 'F', '', '', 'sys:role:add', '', 1),
(4022, 402, '修改角色', 'F', '', '', 'sys:role:edit', '', 2),
(4023, 402, '删除角色', 'F', '', '', 'sys:role:delete', '', 3),
(4024, 402, '分配权限', 'F', '', '', 'sys:role:perm', '', 4),
(403, 400, '菜单管理', 'C', '/admin/system/menu', 'admin/system/SysMenuManageView', 'sys:menu:list', 'Menu', 3),
(4031, 403, '新增菜单', 'F', '', '', 'sys:menu:add', '', 1),
(4032, 403, '修改菜单', 'F', '', '', 'sys:menu:edit', '', 2),
(4033, 403, '删除菜单', 'F', '', '', 'sys:menu:delete', '', 3),
-- 500: 技术实验室
(500, 0, '技术实验室', 'C', '/admin/lab', 'admin/LabView', 'system:lab:view', 'Cpu', 5)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- ============================================================
-- 5. 角色与菜单权限关联表 (sys_role_menu)
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单/权限ID',
  PRIMARY KEY (`role_id`, `menu_id`),
  INDEX `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- 普通用户只绑定仪表盘
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 100)
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`);

-- 超级管理员绑定全部菜单与权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu`
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`);

-- ============================================================
-- 6. 站点顶部导航菜单表 (site_nav)
-- ============================================================
CREATE TABLE IF NOT EXISTS `site_nav` (
  `id` BIGINT NOT NULL COMMENT '导航菜单ID (雪花算法 19 位)',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称 (如: 首页、空间概览、漫游地图)',
  `path` VARCHAR(100) NOT NULL COMMENT '路由跳转路径 (如: /、/overview、/map)',
  `icon` VARCHAR(50) DEFAULT '' COMMENT '图标或Emoji (如: 🏠、🏞️、🗺️、🧭)',
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

-- ============================================================
-- 7. VR 全景分类/园区管理表 (vr_category)
-- ============================================================
CREATE TABLE IF NOT EXISTS `vr_category` (
  `id` BIGINT NOT NULL COMMENT 'VR分类ID (雪花算法 19 位)',
  `name` VARCHAR(50) NOT NULL COMMENT '分类/园区名称',
  `code` VARCHAR(50) NOT NULL COMMENT '英文唯一标识码',
  `map_url` VARCHAR(500) DEFAULT '' COMMENT '导览俯视底图URL (为空时前端自动隐藏导览底图)',
  `description` VARCHAR(255) DEFAULT '' COMMENT '园区/分类简介',
  `sort` INT DEFAULT 0 COMMENT '排序权重 (数字越小越靠前)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0: 禁用, 1: 启用)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`, `deleted`),
  INDEX `idx_sort` (`sort`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VR全景分类与园区表';

INSERT INTO `vr_category` (`id`, `name`, `code`, `map_url`, `description`, `sort`, `status`, `deleted`) VALUES
(2001, '顺峰山公园-西区', 'west_park', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/test/E501map.png', '顺峰山公园西园区，包含美的体育广场、伏波桥、大草地等知名景点', 1, 1, 0),
(2002, '顺峰山公园-东区', 'east_park', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/test/E501map.png', '顺峰山公园东园区，包含顺峰牌坊、宫殿、观音堂等景点', 2, 1, 0),
(2003, '顺峰山龙舟汇', 'dragon_boat', '', '顺峰山公园龙舟汇室内博物馆全景展区（无平面俯瞰图，纯场景漫游）', 3, 1, 0)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `description`=VALUES(`description`), `map_url`=VALUES(`map_url`);

-- ============================================================
-- 8. VR 全景具体点位场景表 (vr_scene)
-- ============================================================
CREATE TABLE IF NOT EXISTS `vr_scene` (
  `id` BIGINT NOT NULL COMMENT '场景ID (雪花算法 19 位)',
  `category_id` BIGINT NOT NULL COMMENT '所属分类ID (关联 vr_category.id)',
  `name` VARCHAR(50) NOT NULL COMMENT '场景名称',
  `panorama_url` VARCHAR(500) NOT NULL COMMENT '360全景原图URL (Cloudflare R2)',
  `preview_url` VARCHAR(500) DEFAULT '' COMMENT '场景缩略图URL (列表与打点预览)',
  `top_percent` DECIMAL(6, 2) NOT NULL DEFAULT 0.00 COMMENT '导览地图打点 Y 轴百分比坐标 (0.00% - 100.00%)',
  `left_percent` DECIMAL(6, 2) NOT NULL DEFAULT 0.00 COMMENT '导览地图打点 X 轴百分比坐标 (0.00% - 100.00%)',
  `initial_deg` INT NOT NULL DEFAULT 0 COMMENT '初始进入视角水平航向偏角 (-180° ~ 180°)',
  `sort` INT DEFAULT 0 COMMENT '排序权重 (数字越小越靠前)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0: 禁用, 1: 启用)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  INDEX `idx_category_id` (`category_id`),
  INDEX `idx_sort` (`sort`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VR全景具体点位场景表';

INSERT INTO `vr_scene` (`id`, `category_id`, `name`, `panorama_url`, `preview_url`, `top_percent`, `left_percent`, `initial_deg`, `sort`, `status`, `deleted`) VALUES
(3001, 2001, '伏波桥', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/fuboqiao/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/fuboqiao/preview.jpg', 32.50, 48.60, -32, 1, 1, 0),
(3002, 2001, '美的体育广场', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/mideaSquare/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/mideaSquare/preview.jpg', 45.20, 28.30, 0, 2, 1, 0),
(3003, 2001, '龙舟馆顶层', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/longzhou_top/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/longzhou_top/preview.jpg', 58.70, 72.10, -45, 3, 1, 0),
(3004, 2003, '龙舟馆入口', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/rukou/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/rukou/preview.jpg', 0.00, 0.00, -32, 1, 1, 0),
(3005, 2003, '龙舟馆中庭', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/zhongjian/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/zhongjian/preview.jpg', 0.00, 0.00, 0, 2, 1, 0)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `panorama_url`=VALUES(`panorama_url`), `preview_url`=VALUES(`preview_url`);
