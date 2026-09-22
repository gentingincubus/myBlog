-- ============================================================
-- 站点顶部导航菜单表 (site_nav)
-- 包含：雪花算法ID (19位)、公共审计字段、逻辑删除
-- ============================================================

CREATE TABLE IF NOT EXISTS `site_nav` (
  `id` BIGINT NOT NULL COMMENT '导航菜单ID (雪花算法 19 位)',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称 (如: 首页、个人介绍、花活工坊)',
  `path` VARCHAR(100) NOT NULL COMMENT '路由跳转路径 (如: /、/about、/tools)',
  `icon` VARCHAR(50) DEFAULT '' COMMENT '图标或Emoji (如: 🏠、👤、🧪、🛠️)',
  `sort` INT DEFAULT 0 COMMENT '排序权重 (数字越小越靠前)',
  `is_blank` TINYINT NOT NULL DEFAULT 0 COMMENT '是否新窗口打开 (0: 否, 1: 是)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID (自动填充)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 (自动填充)',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 (自动填充)',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  INDEX `idx_sort` (`sort`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点顶部导航菜单表';

-- 插入初始导航菜单
INSERT INTO `site_nav` (`id`, `name`, `path`, `icon`, `sort`, `is_blank`, `deleted`) VALUES
(1001, '首页', '/', '🏠', 1, 0, 0),
(1002, '个人介绍', '/about', '👤', 2, 0, 0),
(1003, '花活工坊', '/playground', '🧪', 3, 0, 0),
(1004, 'AI工具箱', '/tools', '🛠️', 4, 0, 0);
