-- ============================================================
-- MyBlog 企业级 RBAC 权限系统初始化脚本
-- 包含：角色表 (sys_role)、用户角色关联表 (sys_user_role)、
--       菜单与权限表 (sys_menu)、角色菜单关联表 (sys_role_menu)
-- ============================================================

USE `myblog`;

-- 强制指定当前会话使用 utf8mb4 字符集，杜绝终端导入时的编码乱码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 0. 清理旧表（若存在）
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_role`;

-- 1. 系统角色表 (sys_role)
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

-- 2. 用户与角色关联表 (sys_user_role)
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`),
  INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和角色关联表';

-- 预置将 admin 用户 (ID: 1) 绑定为超级管理员角色 (ID: 1)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1)
ON DUPLICATE KEY UPDATE `user_id`=VALUES(`user_id`);

-- 3. 菜单与权限规则表 (sys_menu)
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

-- 4. 角色与菜单权限关联表 (sys_role_menu)
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单/权限ID',
  PRIMARY KEY (`role_id`, `menu_id`),
  INDEX `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色和菜单关联表';

-- ============================================================
-- 预置全量菜单树与按钮权限
-- ============================================================

-- 100: 仪表盘 (所有角色包含普通用户默认均可见)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(100, 0, '仪表盘', 'C', '/admin/dashboard', 'admin/DashboardView', 'admin:dashboard:view', 'Odometer', 1)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`);

-- 200: VR 全景中台 (目录)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(200, 0, 'VR 全景中台', 'M', '/admin/vr', '', '', 'View', 2)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`);

-- 201: VR 园区分类管理 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(201, 200, '园区分类管理', 'C', '/admin/vr/category', 'admin/vr/VrCategoryManageView', 'vr:category:list', 'FolderOpened', 1),
(2011, 201, '新增分类', 'F', '', '', 'vr:category:add', '', 1),
(2012, 201, '修改分类', 'F', '', '', 'vr:category:edit', '', 2),
(2013, 201, '删除分类', 'F', '', '', 'vr:category:delete', '', 3)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 202: 全景场景管理 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(202, 200, '全景场景管理', 'C', '/admin/vr/scene', 'admin/vr/VrSceneManageView', 'vr:scene:list', 'PictureFilled', 2),
(2021, 202, '新增场景', 'F', '', '', 'vr:scene:add', '', 1),
(2022, 202, '修改场景', 'F', '', '', 'vr:scene:edit', '', 2),
(2023, 202, '删除场景', 'F', '', '', 'vr:scene:delete', '', 3)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 203: 可视化打点 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(203, 200, '可视化打点', 'C', '/admin/vr/editor', 'admin/vr/VrMapEditorView', 'vr:editor:view', 'LocationInformation', 3),
(2031, 203, '保存打点', 'F', '', '', 'vr:editor:save', '', 1)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 300: 导航管理 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(300, 0, '导航管理', 'C', '/admin/nav', 'admin/NavManageView', 'site:nav:list', 'Compass', 3),
(3001, 300, '新增导航', 'F', '', '', 'site:nav:add', '', 1),
(3002, 300, '修改导航', 'F', '', '', 'site:nav:edit', '', 2),
(3003, 300, '删除导航', 'F', '', '', 'site:nav:delete', '', 3)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 400: 系统管理 (目录)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(400, 0, '系统管理', 'M', '/admin/system', '', '', 'Setting', 4)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`);

-- 401: 用户管理 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(401, 400, '用户管理', 'C', '/admin/system/user', 'admin/system/SysUserManageView', 'sys:user:list', 'User', 1),
(4011, 401, '用户查询', 'F', '', '', 'sys:user:query', '', 1),
(4012, 401, '分配角色', 'F', '', '', 'sys:user:role', '', 2),
(4013, 401, '状态切换', 'F', '', '', 'sys:user:status', '', 3),
(4014, 401, '重置密码', 'F', '', '', 'sys:user:resetPwd', '', 4)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 402: 角色管理 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(402, 400, '角色管理', 'C', '/admin/system/role', 'admin/system/SysRoleManageView', 'sys:role:list', 'Stamp', 2),
(4021, 402, '新增角色', 'F', '', '', 'sys:role:add', '', 1),
(4022, 402, '修改角色', 'F', '', '', 'sys:role:edit', '', 2),
(4023, 402, '删除角色', 'F', '', '', 'sys:role:delete', '', 3),
(4024, 402, '分配权限', 'F', '', '', 'sys:role:perm', '', 4)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 403: 菜单管理 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(403, 400, '菜单管理', 'C', '/admin/system/menu', 'admin/system/SysMenuManageView', 'sys:menu:list', 'Menu', 3),
(4031, 403, '新增菜单', 'F', '', '', 'sys:menu:add', '', 1),
(4032, 403, '修改菜单', 'F', '', '', 'sys:menu:edit', '', 2),
(4033, 403, '删除菜单', 'F', '', '', 'sys:menu:delete', '', 3)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 500: 技术实验室 (菜单)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(500, 0, '技术实验室', 'C', '/admin/lab', 'admin/LabView', 'system:lab:view', 'Cpu', 5)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`);

-- ============================================================
-- 角色与菜单初始绑定：
-- 1. 普通用户 (ID: 2) -> 仅绑定 100 (仪表盘)
-- 2. 超级管理员 (ID: 1) -> 绑定所有菜单和按钮
-- ============================================================

-- 普通用户只绑定仪表盘
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 100)
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`);

-- 超级管理员绑定全部菜单与权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu`
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`);

