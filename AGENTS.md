# Repository Guidelines (myBlog 管理后台)

## 1. 项目定位与架构
本仓库是顺峰全景漫游系统的统一管理控制台（Admin Console），基于 **Vue 3 (Composition API / `<script setup>`)**、**Vite**、**Element Plus**、**Pinia** 与 **Vue Router** 构建。
主代码位于 `src/`：
- `api/`：统一网络请求模块（Axios 封装，带 Token 拦截、全局错误提示、文件直传方法）；
- `views/admin/vr/`：VR 全景中台核心页面：
  - `VrCategoryManageView.vue`：园区分类与全局补地遮罩管理；
  - `VrSceneManageView.vue`：全景场景管理（含 360 原图、客户端 Canvas 自动压制 LQIP 秒开底图、独立补地遮罩）；
  - `VrMapEditorView.vue`：全景地图打点可视化编辑器；
- `components/` & `views/admin/vr/components/`：业务可复用组件（如 `NadirConfigPanel.vue` 脚底补地遮罩配置器）；
- `docs/`：管理端各业务模块说明与变更记录。

---

## 2. 构建与本地运行命令
- `npm run dev`：启动本地 Vite 开发服务器（默认端口：5173，反向代理至后端 `http://localhost:9090`）；
- `npm run build`：执行生产环境静态资源打包构建，产物输出至 `dist/`。

---

## 3. 前端核心开发规范与铁律 ⚡
1. **客户端高性能处理优先**：
   - 全景图上传涉及大图（4K/8K），必须利用浏览器本地 Canvas/Web Worker 进行计算（如自动生成 1024×512、30KB 的 LQIP 秒开贴图），零占用服务器 CPU 与带宽开销；
2. **大图直传云存储**：
   - 严禁把 50MB+ 原图流经应用服务器转发，统一由客户端直传 Cloudflare R2，接口层仅负责保存直链；
3. **组件化与低耦合**：
   - 复杂的配置面板（如补地遮罩配置、地图打点画布）必须独立抽离为高内聚组件，并通过 `v-model` 响应式双向绑定；
4. **UI 规范**：
   - 严格遵循 Element Plus 交互与配色规范，卡片悬停遮罩动效、上传拖拽卡片需保持全站风格统一。

---

## 4. 文档体系维护（`docs/`）规范 📖
任何功能模块的重大新增或交互重构，智能体**必须在 `docs/` 目录下同步更新或追加文档**：
- **文首版本总表铁律**：所有模块设计与交互文档最上方，**必须维护版本变更总表**（列包含：`| 版本 | 日期 | 变更内容 (精炼说明新增/重构了什么功能) | 关联分支 |`），方便人类开发者快速查阅功能履历，下方展开详细交互与逻辑；
- `docs/modules/`：业务模块架构说明（功能清单、状态流转、核心算法设计）；
- `docs/changelog/`：模块变更记录（日期、分支、变更点、用户体验提升）。

---

## 5. 分支开发与协作约束
- 所有开发与修复均基于 `feature/*` 或 `hotfix/*` 分支；
- 提交前必须执行 `npm run build` 确保 0 语法报错、0 构建异常；
- 提交信息采用 Conventional Commits 风格，如：`feat(vr-scene): 支持全景原图自动压制低清秒开底图`。

