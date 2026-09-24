-- ============================================================
-- 顺峰山 VR 全景系统数据表定义
-- 包含：VR分类表 (vr_category) 与 VR全景场景表 (vr_scene)
-- 特性：雪花算法ID (19位)、公共审计字段、逻辑删除、百分比防漂移坐标
-- ============================================================

-- 1. VR 分类 / 园区管理表
CREATE TABLE IF NOT EXISTS `vr_category` (
  `id` BIGINT NOT NULL COMMENT 'VR分类ID (雪花算法 19 位)',
  `name` VARCHAR(50) NOT NULL COMMENT '分类/园区名称 (如: 顺峰山公园-西区、顺峰山公园-东区、顺峰山龙舟汇)',
  `code` VARCHAR(50) NOT NULL COMMENT '英文唯一标识码 (如: west_park, east_park, dragon_boat)',
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

-- 2. VR 全景场景管理表
CREATE TABLE IF NOT EXISTS `vr_scene` (
  `id` BIGINT NOT NULL COMMENT '场景ID (雪花算法 19 位)',
  `category_id` BIGINT NOT NULL COMMENT '所属分类ID (关联 vr_category.id)',
  `name` VARCHAR(50) NOT NULL COMMENT '场景名称 (如: 伏波桥、龙舟馆入口、牌坊)',
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

-- ============================================================
-- 初始演示数据（对应原顺峰山 VR 经典场景）
-- ============================================================

-- 插入 3 个核心分类（西区、东区带地图底图，龙舟汇无地图底图）
INSERT INTO `vr_category` (`id`, `name`, `code`, `map_url`, `description`, `sort`, `status`, `deleted`) VALUES
(2001, '顺峰山公园-西区', 'west_park', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/test/E501map.png', '顺峰山公园西园区，包含美的体育广场、伏波桥、大草地等知名景点', 1, 1, 0),
(2002, '顺峰山公园-东区', 'east_park', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/test/E501map.png', '顺峰山公园东园区，包含顺峰牌坊、宫殿、观音堂等景点', 2, 1, 0),
(2003, '顺峰山龙舟汇', 'dragon_boat', '', '顺峰山公园龙舟汇室内博物馆全景展区（无平面俯瞰图，纯场景漫游）', 3, 1, 0)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

-- 插入场景示例数据（百分比坐标，自适应任何屏幕与缩放）
INSERT INTO `vr_scene` (`id`, `category_id`, `name`, `panorama_url`, `preview_url`, `top_percent`, `left_percent`, `initial_deg`, `sort`, `status`, `deleted`) VALUES
(3001, 2001, '伏波桥', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/fuboqiao/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/fuboqiao/preview.jpg', 32.50, 48.60, -32, 1, 1, 0),
(3002, 2001, '美的体育广场', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/mideaSquare/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/mideaSquare/preview.jpg', 45.20, 28.30, 0, 2, 1, 0),
(3003, 2001, '龙舟馆顶层', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/longzhou_top/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/longzhou_top/preview.jpg', 58.70, 72.10, -45, 3, 1, 0),
(3004, 2003, '龙舟馆入口', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/rukou/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/rukou/preview.jpg', 0.00, 0.00, -32, 1, 1, 0),
(3005, 2003, '龙舟馆中庭', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/zhongjian/yasuo.jpg', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/zhongjian/preview.jpg', 0.00, 0.00, 0, 2, 1, 0)
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`);

