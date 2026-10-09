# 变更记录: 管理后台新增 Cloudflare R2 存储桶管理工作台

## 文档版本

| 版本 | 日期 | 变更内容 | 关联分支 |
| :--- | :--- | :--- | :--- |
| v1.0 | 2026-10-09 | 新增「存储桶管理工作台」（`StorageManageView.vue`）：实现已用容量与 10GB 免费配额可视化、孤儿废图高亮预警、一键批量物理清理与单文件手动删除。 | `feature/20261009_vr管理相关` |

---

## 变更详情

1. **新建 API 请求模块**:
   - `src/api/storage.js`: 封装 `getStorageStatsApi`、`listStorageFilesApi`、`deleteStorageFileApi`、`cleanOrphanFilesApi`。
2. **新建存储管理页面**:
   - `src/views/admin/system/StorageManageView.vue`:
     - 顶部存储容量看板、对象总数健康度看板、孤儿废图预警看板；
     - 业务分类过滤选择器（含全景原图、低清预览、补地遮罩Logo、地图底图、轮播图、头像）；
     - 孤儿垃圾文件专属过滤开关；
     - 文件列表支持大图预览、Key/直链一键复制、排序与单文件物理删除；
     - 一键批量清理孤儿废图（带危险弹窗二次确认与清理结果通知）。
3. **系统路由注册**:
   - `src/router/index.js`: 在系统管理下注册 `/admin/system/storage` 路由，关联 `system:storage:view` 权限。
