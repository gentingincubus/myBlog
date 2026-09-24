# 🌟 项目开发接力与上下文全景文档 (Context Handover)

> **致接力的 AI 助手 / 开发者**：  
> 本文档是整个项目的核心上下文快照。阅读本文档即可 100% 掌握系统架构、已完成进度、核心设计决策、当前在线状态及后续待办，无需从头重构思路。

---

## 📌 1. 项目背景与目标
- **目标**：将原基于 Vue 2 + Three.js 的顺峰山公园 VR 全景系统，重构并无缝迁移至个人博客站（`gentingincubus.com`）。
- **工程结构**：
  - `backend/`：Spring Boot 3 + Java 21 + MyBatis-Plus + Redis + Cloudflare R2 SDK (AWS S3 Java v2)
  - `frontend/`：Vue 3 + Vite + Pinia + Element Plus + Three.js (`0.186.0`) + `@tweenjs/tween.js` (`25.0.0`)
  - `deploy/`：Docker Compose 一键部署（MySQL 8.0、Redis 7、Temurin 21 JRE、Nginx）
  - `.github/workflows/deploy.yml`：CI/CD 自动化流水线（push main 自动打包、SCP 上传、SSH 重载）

---

## 🏗️ 2. 核心架构与设计决策

### ① 对象存储（Cloudflare R2）
- **Bucket**：`myblog-vr`
- **公共直链域名**：`https://vr.gentingincubus.com`
- **目录隔离**：
  - 园区俯瞰底图：`vr/map/`
  - 360° 球形全景原图（4K/8K）：`vr/panorama/`
  - 场景缩略图：`vr/preview/`

### ② 坐标体系设计（防漂移百分比）
- 底图打点坐标绝对**禁止使用固定像素 (px)**，统一采用 `top_percent` (0~100) 和 `left_percent` (0~100) 的 `DECIMAL(5,2)` 浮点百分比存储。
- 无论用户屏幕尺寸缩放、窗口如何形变，打点永远钉死在底图的相对地理坐标上。

### ③ 数据库安全规范
- 生产环境 ECS MySQL 8.0 仅监听回环地址 `127.0.0.1:3306:3306`，坚决杜绝暴露公网。
- 本地 Navicat 连接通过 **SSH 隧道 (Port 22)** 直连服务器。

### ④ 后端代码模块化规范
- 所有 VR 相关的类统一归类于 `vr` 子包下，杜绝散落根目录：
  - 实体：`entity/vr/VrCategory.java`, `entity/vr/VrScene.java`
  - DTO/VO：`dto/vr/`, `dto/vr/vo/`
  - Mapper：`mapper/vr/VrCategoryMapper.java`, `mapper/vr/VrSceneMapper.java`
  - Service：`service/vr/`
  - Controller：`controller/vr/VrCategoryController.java`, `controller/vr/VrSceneController.java`

---

## 🚀 3. 已完成进度 (Phase 1 ~ Phase 5 已 100% 完成)

1. **Phase 1 (存储集成)**：Cloudflare R2 通道、直传 API、单元测试全部通过。
2. **Phase 2 (后端与数据)**：
   - 数据库表结构 [`docs/sql/vr_tables.sql`](file:///docs/sql/vr_tables.sql) 完成并在服务器执行。
   - 所有公开查询接口在 [`WebMvcConfig.java`](file:///backend/src/main/java/org/example/backend/config/WebMvcConfig.java) 中配置了 JWT 白名单（无需登录公开访问）：
     - `/api/vr/category/list`
     - `/api/vr/category/detail/*`
     - `/api/vr/scene/list`
     - `/api/vr/scene/detail/*`
3. **Phase 3 (后台管理与打点编辑器)**：
   - `VrCategoryManageView.vue`：园区分类管理（含 R2 底图直传、无底图园区自适应）。
   - `VrSceneManageView.vue`：全站点位列表（支持 4K 全景直传、初始航向角设定）。
   - `VrMapEditorView.vue`：**Figma/GIS 级可视化打点画布**，支持鼠标滚轮缩放、拖拽平移、点位拖拽定位、单击底图快速打点、实时百分比显示与批量保存。
4. **Phase 4 & 5 (前台 720° VR 全景巡游播放器)**：
   - `VrPlayerView.vue`：
     - 四种镜头视角模式自由切换：普通视角、小行星 (Little Planet)、鱼眼 (Fisheye)、水晶球 (Crystal Ball)。
     - 优雅的开场俯冲下坠动画（FOV 130° -> 75°）。
     - 右上角可折叠半透明园区地图，带与全景镜头实时联动的**小雷达航向扇形**。
     - 场景切换 TWEEN 透明度淡入淡出，且**严格执行 `texture.dispose()` 释放 WebGL 显存**，杜绝手机与浏览器内存崩溃。
     - 博客首页（Area 3）已接入西区/东区直达入口。

---

## ⚡ 4. 关键线上状态与注意事项

### ① 为什么线上接口刚更新时报 401？
- **原因**：GitHub Actions 脚本中执行 `docker compose up -d` 时，因为 `myblog-backend` 容器镜像与配置未变，Docker 不会重启已运行的容器，导致 Java 进程在内存中仍然跑着旧版 JAR（未含新白名单）。
- **永久修复**：已经在 `.github/workflows/deploy.yml` 的部署脚本中添加了 `docker compose restart backend`。
- **当前服务器临时生效命令**：
  ```bash
  sudo docker restart myblog-backend
  ```

### ② 本地私密配置文件 (Git 忽略，需本地手动创建)
在 `backend/src/main/resources/application-local.properties`：
```properties
spring.datasource.password=Genting#89?
spring.data.redis.password=123456
r2.access-key-id=da6dedd67486cc53cb2524fafa6a3c1d
r2.secret-access-key=32523ab3ddf7981aff6751defdb842759adc01d4861f857c75766b21bb18b059
```

---

## 📋 5. 接力待办事项 (Next Steps)
1. **线上验证**：
   - 在服务器执行 `sudo docker restart myblog-backend` 后，验证 `https://api.gentingincubus.com/api/vr/category/list` 返回 200。
   - 访问前端页面，测试分类加载、场景播放、可视化打点编辑器。
2. **后续扩展**：
   - 如需为全景球体内增加 3D 浮动热点图标（点击地标直接切到下一个场景）。
   - 如需增加全景背景音乐播放功能（BGM）。
