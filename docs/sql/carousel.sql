-- ============================================================
-- 首页第二页 3D 毛玻璃轮播图 (site_carousel) 数据表与权限配置
-- 包含：数据表创建、初始精美顺峰山卡片数据、RBAC 菜单与权限绑定
-- ============================================================

USE `myblog`;

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 1. 创建首页轮播图表
CREATE TABLE IF NOT EXISTS `site_carousel` (
  `id` BIGINT NOT NULL COMMENT '轮播ID (雪花算法 19 位)',
  `title` VARCHAR(100) NOT NULL COMMENT '轮播标题',
  `subtitle` VARCHAR(200) DEFAULT '' COMMENT '副标题/简短标语',
  `cover_url` VARCHAR(500) NOT NULL COMMENT '封面图片URL (Cloudflare R2 直链)',
  `content` MEDIUMTEXT NOT NULL COMMENT '卡片背面富文本详细介绍 (Markdown 格式)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重 (数字越小越靠前)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1: 启用, 0: 禁用)',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识 (0: 正常, 1: 已删除)',
  PRIMARY KEY (`id`),
  INDEX `idx_sort` (`sort`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图表';

-- 2. 插入初始顺峰山精选卡片数据 (带有丰富的 Markdown 图文介绍)
INSERT INTO `site_carousel` (`id`, `title`, `subtitle`, `cover_url`, `content`, `sort`, `status`, `deleted`) VALUES
(6001, '顺峰山公园 · 中华第一牌坊', '顺德之门，气势磅礴的岭南建筑丰碑', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/fuboqiao/yasuo.jpg', '# 顺峰山公园 · 中华第一牌坊\n\n> 顺峰山牌坊享有“**中华第一牌坊**”之美誉，坐落于顺德大良顺峰山公园入口处，体量恢宏，气度万千。\n\n---\n\n### 🏛️ 建筑特色与艺术构造\n- **三跨拱券结构**：主跨雄阔，翼跨对称，整座牌坊高 38 米，宽 88 米，气势磅礴。\n- **石雕与彩绘**：融入了大量岭南传统石雕艺术，雕刻有龙凤呈祥、百鸟朝凤等生动图案。\n- **琉璃覆顶**：金黄色琉璃瓦在阳光照耀下熠熠生辉，与青云湖水倒影交相辉映。\n\n### 🌿 漫游体验推荐\n1. **晨曦初照**：清晨登临牌坊广场，朝霞映照金顶，是绝佳摄影打卡机位。\n2. **全景漫步**：从牌坊向内步入，沿着青云湖环湖绿道漫步，微风徐来，心旷神怡。', 1, 1, 0),

(6002, '青云塔与桂畔湖 · 湖光塔影', '凌霄矗立，俯瞰顺德秀美山河', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/leftMap/mideaSquare/yasuo.jpg', '# 青云塔与桂畔湖 · 湖光塔影\n\n> 青云塔耸立于神步山巅，始建于明代万历年间，为顺德八景之一“**青云挺秀**”。\n\n---\n\n### ✨ 胜景特色\n- **八角七层阁楼式**：砖石垒砌，古朴雄浑，历经数百年风雨依然傲立。\n- **湖光相映**：桂畔湖碧波荡漾，与青云古塔在碧水微澜中形成“双塔映波”的经典画卷。\n- **自然生机**：湖畔红杉挺立，白鹭翔集，是顺峰山最具生态韵味的湿地核心区。\n\n> 💡 *小提示：支持通过顶部 VR 漫游系统直达青云塔下俯瞰全景。*', 2, 1, 0),

(6003, '顺峰山龙舟汇 · 水上文化方舟', '现代建筑与非遗传承的水上交响', 'https://1967.oss-cn-guangzhou.aliyuncs.com/image/VR/dragonBoat/rukou/yasuo.jpg', '# 顺峰山龙舟汇 · 水上文化方舟\n\n> 由清华大学建筑设计团队操刀打造，宛若一艘巨型龙舟静卧于莫家桥畔湖水之上。\n\n---\n\n### 🚣‍♂️ 场馆亮点\n- **水上方舟造型**：全钢构架与现代木纹外立面相得益彰，犹如漂浮在湖面的传统龙舟。\n- **多维互动展区**：馆内集中陈列顺德五人龙舟、传统龙首雕刻与国际锦标赛奖杯。\n- **全景沉浸漫游**：支持通过本站 720° VR 全景系统穿梭于龙舟汇中庭与屋顶观景平台。', 3, 1, 0)
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`), `subtitle`=VALUES(`subtitle`), `cover_url`=VALUES(`cover_url`), `content`=VALUES(`content`);

-- 3. 注册 RBAC 菜单与按钮权限 (挂载于后台菜单中)
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `perms`, `icon`, `sort`) VALUES
(600, 0, '轮播管理', 'C', '/admin/carousel', 'admin/carousel/CarouselManageView', 'site:carousel:list', 'Film', 25),
(6001, 600, '新增轮播', 'F', '', '', 'site:carousel:add', '', 1),
(6002, 600, '修改轮播', 'F', '', '', 'site:carousel:edit', '', 2),
(6003, 600, '删除轮播', 'F', '', '', 'site:carousel:delete', '', 3)
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`), `perms`=VALUES(`perms`);

-- 4. 自动授权给超级管理员 (ID: 1)
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 600),
(1, 6001),
(1, 6002),
(1, 6003)
ON DUPLICATE KEY UPDATE `role_id`=VALUES(`role_id`);
