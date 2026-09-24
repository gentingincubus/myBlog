# 顺峰山公园 VR 全景系统重构详录（Phase 2 ~ Phase 5 完整设计与实现）

> **文档说明**：  
> 本文档完整收录了今天在开发过程中关于 **Phase 2（后端架构）**、**包结构重构**、**Phase 3（中台管理与可视化打点编辑器）** 以及 **Phase 4 & 5（Three.js 全景播放器）** 的全部技术实现细节与设计亮点，供回家后深入学习与查阅。

---

## 📑 目录
- [一、 Phase 2：数据库模型设计与后端 CRUD 核心接口](#一-phase-2数据库模型设计与后端-crud-核心接口)
- [二、 后端代码分包重构：规范化模块收敛](#二-后端代码分包重构规范化模块收敛)
- [三、 Phase 3：VR 全景中台管理与 Figma 级可视化打点编辑器](#三-phase-3vr-全景中台管理与-figma-级可视化打点编辑器)
- [四、 Phase 4 & 5：Vue 3 + Three.js 现代化 720° 全景播放器](#四-phase-4--5vue-3--threejs-现代化-720-全景播放器)
- [五、 全链路功能与路由对照总表](#五-全链路功能与路由对照总表)

---

## 一、 Phase 2：数据库模型设计与后端 CRUD 核心接口

### 1. 数据库脚本设计 (`docs/sql/vr_tables.sql`)
- **`vr_category`（全景分类/园区表）**：
  - **核心字段**：`id`（雪花算法）、`name`（如“顺峰山公园-西区”）、`code`（英文编码 `west_park`）、`map_url`（**导览底图 URL，支持留空**）、`description`、`sort`、`status`，以及公共审计与逻辑删除字段。
  - **地图可选支持**：若底图为空（如室内馆“顺峰山龙舟汇”），前端会自动隐藏地图导览按钮与弹窗，自适应纯漫游模式。
- **`vr_scene`（全景场景点位表）**：
  - **核心字段**：`category_id`、`name`（如“伏波桥”）、`panorama_url`（360 全景原图）、`preview_url`（缩略图）、`top_percent`（Y 轴百分比坐标 `0.00% ~ 100.00%`）、`left_percent`（X 轴百分比坐标 `0.00% ~ 100.00%`）、`initial_deg`（初始航向偏角 `-180° ~ 180°`）、`sort`、`status`。
  - **🌟 百分比防漂移坐标设计**：彻底废弃老代码的固定 `px` / `rem`，无论用户屏幕分辨率是 1080P、2K 还是手机屏幕，缩放地图时打点永远钉死在底图的相对地理坐标上。
- **内置初始种子数据**：内置了西园区、东园区、龙舟汇 3 个分类及 5 个经典场景示例。

### 2. 后端实体与数据传输模型（Entity / DTO / VO）
- **实体类**：
  - `VrCategory.java`：继承 `BaseEntity`，受雪花 ID 与自动审计字段管理。
  - `VrScene.java`：定义了 `topPercent` 与 `leftPercent`（`BigDecimal` 高精度浮点百分比）。
- **DTO 请求参数与严格校验**：
  - `VrCategoryReqDto.java`、`VrCategoryQueryDto.java`
  - `VrSceneReqDto.java`、`VrSceneQueryDto.java`（使用 Jakarta Validation 限制百分比 `0.00~100.00`，角度 `-180~180`）。
- **VO 视图展示对象（带内存流水线组装）**：
  - `VrCategoryVo.java`：附带聚合字段 `sceneCount`（该分类下的场景点位总数）。
  - `VrSceneVo.java`：跨表装配 `categoryName`，方便管理端展示及前端路径面包屑。

### 3. 业务服务层（MyBatis-Plus + Redis 旁路缓存）
- **Mapper 接口**：`VrCategoryMapper.java` 与 `VrSceneMapper.java`。
- **业务实现亮点**：
  - `VrCategoryServiceImpl.java`：分类增删改查；删除分类前主动**检查并拦截含有未删除场景的分类**，杜绝产生孤儿场景；整合全量分类缓存 `vr:category:list:all`。
  - `VrSceneServiceImpl.java`：场景点位增删改查；按分类独立缓存 `vr:scene:list:{categoryId}`；任何场景增删改自动通过 `CacheUtils.deleteAfterCommit` 清理相关分类及全量缓存。

### 4. RESTful 接口与白名单权限管理
- **公开白名单**（游客与前台 VR 播放器无需登录即可访问）：
  - `GET /api/vr/category/list`
  - `GET /api/vr/category/detail/{id}`
  - `GET /api/vr/scene/list`（支持带 `?categoryId=xxx`）
  - `GET /api/vr/scene/detail/{id}`
- **受保护接口**（后台管理必须携带 JWT Token 登录凭证）：
  - `POST /api/vr/category`、`PUT /api/vr/category/{id}`、`DELETE /api/vr/category/{id}`
  - `POST /api/vr/scene`、`PUT /api/vr/scene/{id}`、`DELETE /api/vr/scene/{id}`
  - `PUT /api/vr/scene/batch-update-coords`（打点编辑器批量保存点位坐标）

### 5. 全局异常处理优化 (`GlobalExceptionHandler.java`)
- 增加了针对 Spring Boot 3 `NoResourceFoundException` 的专门拦截器，当扫描器或浏览器访问未注册静态路径（如根路径或 favicon）时，以 `404` 优雅返回，不再向后端控制台刷出冗长的红字错误堆栈。

---

## 二、 后端代码分包重构：规范化模块收敛

为了保持工程整洁，防止日后文件过多难以检索，对所有 VR 相关的代码进行了统一的子包归类收敛：

```text
backend/src/main/java/org/example/backend/
├── entity/
│   ├── BaseEntity.java          # 🌟 特殊例外：通用实体基类（公共审计/雪花ID）
│   ├── SiteNav.java             # 导航实体
│   ├── SysUser.java             # 用户实体
│   └── vr/                      # 📁 VR 专属实体
│       ├── VrCategory.java      # VR 分类与园区实体
│       └── VrScene.java         # VR 场景点位实体
│
├── dto/
│   ├── BasicResponse.java       # 🌟 特殊例外：全局通用统一响应体
│   ├── LoginReqDto.java
│   ├── SiteNavReqDto.java
│   └── vr/                      # 📁 VR 专属入参与出参
│       ├── VrCategoryReqDto.java
│       ├── VrCategoryQueryDto.java
│       ├── VrSceneReqDto.java
│       ├── VrSceneQueryDto.java
│       └── vo/                  # 📁 VR 视图展示对象
│           ├── VrCategoryVo.java
│           └── VrSceneVo.java
│
├── mapper/
│   ├── SiteNavMapper.java
│   ├── SysUserMapper.java
│   └── vr/                      # 📁 VR 专属 Mapper
│       ├── VrCategoryMapper.java
│       └── VrSceneMapper.java
│
├── service/
│   ├── ISiteNavService.java
│   ├── ISysUserService.java
│   └── vr/                      # 📁 VR 业务接口与实现
│       ├── IVrCategoryService.java
│       ├── IVrSceneService.java
│       └── impl/
│           ├── VrCategoryServiceImpl.java
│           └── VrSceneServiceImpl.java
│
├── controller/
│   ├── AuthController.java
│   ├── SiteNavController.java
│   └── vr/                      # 📁 VR 接口控制器
│       ├── VrCategoryController.java
│       └── VrSceneController.java
│
backend/src/test/java/org/example/backend/
└── vr/                          # 📁 VR 专属测试用例
    └── VrDtoValidationTest.java
```

- **自动包扫描机制**：启动类 `BackendApplication.java` 上的 `@MapperScan("org.example.backend.mapper")` 和 Spring `@ComponentScan` 均支持深度包递归，因此新增的 `vr` 子包自动无缝注入。

---

## 三、 Phase 3：VR 全景中台管理与 Figma 级可视化打点编辑器

### 1. 前端 API 模块设计 (`src/api/vr.js`)
- 封装了园区分类 CRUD、场景点位 CRUD 以及坐标批量更新接口。
- **云端存储直传 (Cloudflare R2)**：`uploadImage(file, type)`，封装了 `vr_map`（导览底图）、`vr_panorama`（360 全景原图）、`vr_preview`（缩略微缩图）的直传能力，上传后直接返回 Cloudflare CDN 直链。

### 2. VR 园区分类管理 (`VrCategoryManageView.vue`)
- **多条件检索**：园区名称模糊搜索、英文编码标识搜索、启用/禁用状态筛选。
- **底图预览与管理**：
  - 若配置了底图，表格内展示微缩图并支持大图查看；
  - 若未配置底图（如纯展馆漫游模式），显示友好标签 `无底图(纯漫游)`。
- **下属点位聚合联动**：显示各个分类下已有的点位数，点击标签直接带参跳转到对应场景列表。
- **直接上传底图到 R2**：新增/编辑表单内置直传上传器，上传成功后自动填入 Cloudflare R2 直链并实时预览，无需手动复制粘贴 URL。
- **快捷入口**：对于有底图的分类，表格操作列提供一键直达“**打点编辑**”的快捷按钮。

### 3. VR 全景场景点位管理 (`VrSceneManageView.vue`)
- **分类联动查询**：支持按园区分类下拉筛选、场景名模糊搜索。
- **高阶全景与缩略图上传**：
  - **360 全景原图**：内置大图上传器，支持 4K/8K 等距柱状投影贴图直传 R2。
  - **场景缩略图**：用于前端底部轮播抽屉和打点卡片微缩图。
- **防漂移百分比坐标展示**：表格直接展示 `X: 48.60%`、`Y: 32.50%` 以及初始进入航向角（如 `-32°`）。
- **定位打点一键跳转**：表格每行均有“**定位打点**”按钮，点击后自动携场景 ID 打开打点编辑器并居中高亮该点位。

### 4. 🌟 Figma/GIS 级可视化导览打点编辑器 (`VrMapEditorView.vue`)
这是 Phase 3 研发难度最高的核心组件：
- **无限平滑画布与视口操控**：
  - **滚轮无级缩放**：支持 `20% ~ 400%` 平滑缩放，以鼠标光标所在点为中心进行自适应位移补偿。
  - **画布自由平移**：按住鼠标左键空白处或按住鼠标中键拖动画布，交互体验对标 Figma / Photoshop。
  - **一键自适应居中**：提供 `适应窗口`、`100% 原始视距` 与 `重置视野` 快捷按钮。
- **实时百分比打点与拖拽互动**：
  - **实时光标坐标雷达**：顶部实时换算并显示鼠标当前在底图上的百分比坐标 `X: xx.xx% | Y: xx.xx%`。
  - **图钉直接拖拽**：在地图上直接按住图钉拖动，释放后自动更新该场景的百分比坐标。
  - **点击地图任意处传送**：在左侧列表中选定某个场景后，在地图任意位置单击，图钉即刻飞到该点。
  - **左侧场景抽屉聚焦**：点击场景抽屉的聚焦按钮，画布自动平移放大并将视野中心对准该图钉。
  - **雷达动态光晕**：选中的点位带有 CSS3 动态脉冲光晕，方便辨别定位。
- **脏数据检测与批量安全保存**：
  - 当在地图上调整点位后，点位与顶部工具栏会触发“**有未保存的改动**”警示灯。
  - 点击“**保存所有点位坐标**”按钮，通过专门的批量接口一次性落盘，并自动失效 Redis 缓存。
- **无底图场景优雅容错**：
  - 若切换到无底图的园区（如顺峰山龙舟汇），自动呈现空状态指引，避免报错。

### 5. 侧边栏与路由集成
- `AdminLayout.vue`：侧边栏新增 **VR 全景中台** 折叠子菜单（含 Compass 图标），支持全链路面包屑导航。
- `router/index.js`：注册了 `/admin/vr/category`、`/admin/vr/scene`、`/admin/vr/editor` 3 条路由，受 JWT 全局守卫严密保护。

---

## 四、 Phase 4 & 5：Vue 3 + Three.js 现代化 720° 全景播放器

### 1. 前台全景播放器页面 (`VrPlayerView.vue`)
- **数据驱动架构（彻底告别硬编码）**：
  - 自动调用后端 `/api/vr/category/list` 获取启用的园区分类；
  - 自动调用 `/api/vr/scene/list?categoryId=xxx` 获取场景点位与全景图片；
  - 支持路由参数直达，如 `/vr?code=west_park` 或 `/vr?categoryId=2001&sceneId=3001`。
- **开场沉浸式俯冲入场动效**：
  - 加载完成后，镜头首先处于 140° 超广角俯瞰小行星位置；
  - 随后通过 TWEEN.js 自动平滑俯冲推进至 75° 人眼视角水平视界（耗时 3.2s），带来类似 Google Earth / 高端文旅 VR 的开场仪式感。
- **4 种全景透视视角平滑切换**：
  - **正常视角（75°）**：经典人眼沉浸全景漫游；
  - **小行星视角（140°）**：球状微缩迷你小星球；
  - **鱼眼视角（100°）**：带轻微球体弧度的广角透视；
  - **水晶球视角（75°，外部远摄）**：将镜头拉至球体之外，俯瞰整个全景水晶球。
- **🌟 WebGL 显存泄露防御（彻底解决老版本手机白屏刷新崩溃）**：
  - **双球体交叉渐变转场（Cross-Fade）**：旧场景与新场景通过两个球体透明度（Opacity）平滑过渡（1.2s），视觉零闪烁；
  - **严格显存生命周期回收**：场景切换完毕的瞬间，自动触发 `oldSphere.geometry.dispose()`、`oldTexture.dispose()` 与 `oldMaterial.dispose()`，**彻底从 GPU 显存中擦除旧贴图**，显存常驻峰值恒定控制在 1~2 张贴图，低配手机长时间连续切换也不会发生 OOM 白屏。
- **导览地图雷达偏角与实时指南针跟随**：
  - 悬浮导览地图中，每个打点精准居于 `leftPercent%`、`topPercent%`；
  - **动态雷达导向锥**：当前激活的点位上附带半透明扇形雷达锥，当你在 360° 全景中转动鼠标或滑动手机屏幕时，**地图上的雷达锥实时顺时针同步旋转**，实时指明当前面向的方向；
  - 点击地图任意图钉，镜头即可平滑切入该场景。
- **自适应与优雅容错**：
  - 若所选园区**未配置导览地图底图**（如纯室内展馆“顺峰山龙舟汇”），右上角“地图”按钮与弹窗自动隐藏，前台自适应切换为纯漫游模式；
  - 底部提供磨砂玻璃质感的**场景缩略图横向轮播抽屉**，支持一键折叠/展开；
  - 支持**自动巡航**旋转开关、**背景音乐 BGM** 开关（黑胶旋转动效）、**一键全屏**模式。
- **主页联动 (`HomeView.vue`)**：
  - 首页区域 3（顺峰山地图区域）的“青云湖地图”和“桂畔湖地图”已从原本的提示弹窗直连 `/vr?code=west_park` 和 `/vr?code=east_park`。

---

## 五、 全链路功能与路由对照总表

| 模块 | 对应路径 | 核心功能 |
| :--- | :--- | :--- |
| **全景播放器** | [`/vr`](file:///frontend/src/views/vr/VrPlayerView.vue) | 720° 场景漫游、4 种视角切换、开场俯冲、雷达指南针地图、BGM、自动巡航、显存防泄露 |
| **可视化打点编辑器** | [`/admin/vr/editor`](file:///frontend/src/views/admin/vr/VrMapEditorView.vue) | Figma 级无限画布、滚轮缩放、拖拽打点、点击任意处传送、实时百分比光标、批量保存 |
| **VR 园区分类管理** | [`/admin/vr/category`](file:///frontend/src/views/admin/vr/VrCategoryManageView.vue) | 园区增删改查、底图直传 Cloudflare R2、无底图展馆模式自适应、下属场景保护 |
| **VR 场景点位管理** | [`/admin/vr/scene`](file:///frontend/src/views/admin/vr/VrSceneManageView.vue) | 场景增删改查、4K 全景原图/缩略图直传 R2、防漂移百分比坐标维护、一键跳转编辑器 |
| **对象存储与接口** | `/api/upload` & `/api/vr/*` | Spring Boot 3 + AWS S3 SDK v2 + Redis 旁路缓存 + MySQL 8.0 表结构 |
