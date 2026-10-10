<template>
  <div class="vr-scene-manage-page">
    <el-card shadow="never" class="table-card">
      <!-- 搜索筛选栏 -->
      <div class="filter-bar">
        <el-form :inline="true" :model="queryForm" class="query-form" @submit.prevent>
          <el-form-item label="所属园区">
            <el-select v-model="queryForm.categoryId" placeholder="全部园区" clearable style="width: 180px;"
              @change="handleQuery">
              <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="场景名称">
            <el-input v-model="queryForm.name" placeholder="搜索场景名称" clearable prefix-icon="Search"
              @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="启用状态">
            <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 110px;">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
            <el-button icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <div class="action-buttons">
          <el-button type="warning" icon="Compass" @click="goToMapEditor(queryForm.categoryId)">
            可视化打点编辑器
          </el-button>
          <el-button type="success" icon="Plus" v-hasPermi="['vr:scene:add']" @click="openAddDialog">新增场景点位</el-button>
        </div>
      </div>

      <!-- 场景数据表格 -->
      <el-table v-loading="loading" :data="sceneList" stripe border style="width: 100%" class="custom-table">
        <el-table-column prop="id" label="点位 ID" width="185" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="info">{{ row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="缩略图" width="90" align="center">
          <template #default="{ row }">
            <div class="thumb-container">
              <el-image v-if="row.previewUrl" :src="row.previewUrl"
                :preview-src-list="[row.previewUrl, row.panoramaUrl]" fit="cover" class="thumb-img"
                preview-teleported />
              <span v-else class="no-thumb">无图</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="场景名称与所属园区" min-width="180">
          <template #default="{ row }">
            <div class="scene-info-cell">
              <strong>{{ row.name }}</strong>
              <el-tag size="small" type="info" effect="plain" class="cat-tag">
                {{ row.categoryName || '未分配' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="全景与多分辨率瓦片 (R2)" min-width="240">
          <template #default="{ row }">
            <div class="pano-col-cell">
              <a :href="row.panoramaUrl" target="_blank" class="panorama-link" :title="row.panoramaUrl">
                <el-icon>
                  <PictureFilled />
                </el-icon>
                <span class="url-text">{{ row.panoramaUrl }}</span>
              </a>
              <div class="lqip-badge-wrap">
                <el-tag v-if="row.lowResUrl" size="small" type="success" effect="light" class="lqip-tag">
                  ⚡ 秒开就绪
                </el-tag>
                <el-tag v-else size="small" type="warning" effect="light" class="lqip-tag">
                  未生成秒开图
                </el-tag>

                <!-- 🌟 瓦片状态与实时进度展示 -->
                <!-- 情况 1: 瓦片生成中 (展开微型动态进度条与步骤，点击可查看看板) -->
                <div
                  v-if="row.tileStatus === 1"
                  class="tiling-progress-pill"
                  title="点击打开详细切片监控看板"
                  @click="openTileProgressDialog(row)"
                >
                  <div class="pill-top">
                    <span class="pill-label">
                      <el-icon class="is-loading"><Loading /></el-icon>
                      切片中
                    </span>
                    <span class="pill-percent">{{ row._progress?.percent || 0 }}%</span>
                  </div>
                  <el-progress
                    :percentage="row._progress?.percent || 0"
                    :stroke-width="4"
                    :show-text="false"
                    class="pill-progress-bar"
                  />
                  <span class="pill-step-text" :title="row._progress?.step">
                    {{ row._progress?.step || '提交处理中...' }}
                  </span>
                </div>

                <!-- 情况 2: 瓦片切片失败 (显眼警示，点击弹窗查看精准失败原因与重试) -->
                <el-tag
                  v-else-if="row.tileStatus === 3"
                  size="small"
                  type="danger"
                  effect="plain"
                  class="lqip-tag clickable-tag"
                  title="切片异常中止，点击查看失败原因与诊断"
                  @click="openTileProgressDialog(row)"
                >
                  <el-icon><WarningFilled /></el-icon> 切片失败 · 看原因
                </el-tag>

                <!-- 情况 3: 瓦片已就绪 -->
                <el-tag
                  v-else-if="row.tileStatus === 2 || row.hasTiles === 1"
                  size="small"
                  type="success"
                  effect="plain"
                  class="lqip-tag clickable-tag"
                  title="瓦片网格已就绪，点击查看瓦片配置"
                  @click="openTileProgressDialog(row)"
                >
                  🧩 瓦片已就绪
                </el-tag>

                <!-- 情况 4: 未切片 -->
                <el-tag v-else size="small" type="info" effect="plain" class="lqip-tag">
                  未切片
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="地图坐标 (防漂移百分比)" width="190" align="center">
          <template #default="{ row }">
            <div class="coord-tag-group">
              <el-tag size="small" type="primary" effect="light">X: {{ row.leftPercent }}%</el-tag>
              <el-tag size="small" type="success" effect="light">Y: {{ row.topPercent }}%</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="初始航向角" width="110" align="center">
          <template #default="{ row }">
            <el-tag effect="plain" type="warning">{{ row.initialDeg }}°</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="80" align="center" />

        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="290" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="warning" link size="small" icon="Location" @click="goToMapEditor(row.categoryId, row.id)">
              定位打点
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              icon="Grid"
              :disabled="row.tileStatus === 1"
              :loading="row._generatingTiles"
              v-hasPermi="['vr:scene:edit']"
              @click="handleGenerateTiles(row)"
            >
              {{ row.tileStatus === 1 ? '切片中' : (row.hasTiles === 1 ? '重切瓦片' : '生成瓦片') }}
            </el-button>
            <el-button type="primary" link size="small" icon="Edit" v-hasPermi="['vr:scene:edit']"
              @click="openEditDialog(row)">
              编辑
            </el-button>
            <span v-hasPermi="['vr:scene:delete']">
              <el-popconfirm :title="`确定删除场景【${row.name}】吗？`" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button type="danger" link size="small" icon="Delete">删除</el-button>
                </template>
              </el-popconfirm>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增 / 修改 对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑 VR 场景点位' : '新增 VR 场景点位'" width="780px" destroy-on-close
      :close-on-click-modal="false" class="custom-dialog">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px" class="dialog-form">
        <el-form-item label="所属园区" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择所属 VR 园区/分类" style="width: 100%;">
            <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="场景名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：伏波桥、美的体育广场、牌坊" maxlength="50" show-word-limit />
        </el-form-item>

        <!-- 360 全景原图上传 (Element 规范卡片，隐藏原始 URL) -->
        <el-form-item label="360 全景原图" prop="panoramaUrl">
          <div class="custom-upload-wrapper">
            <!-- 已上传状态：展示全景贴图微缩卡片，支持放大预览、点击重新上传、删除 -->
            <div
              v-if="form.panoramaUrl"
              class="upload-image-card pano-card"
              v-loading="uploadingPano"
              element-loading-text="正在上传..."
              element-loading-background="rgba(255, 255, 255, 0.85)"
            >
              <el-image :src="form.panoramaUrl" fit="cover" class="card-img" :preview-src-list="[form.panoramaUrl]"
                preview-teleported />
              <div class="card-mask">
                <div class="mask-action-list">
                  <span class="mask-action-btn" title="查看原图大图" @click="openImagePreview(form.panoramaUrl)">
                    <el-icon :size="16">
                      <ZoomIn />
                    </el-icon>
                    <span>预览</span>
                  </span>
                  <el-upload
                    class="reupload-trigger"
                    :show-file-list="false"
                    :http-request="handlePanoramaUpload"
                    :disabled="uploadingPano"
                    accept="image/*"
                  >
                    <span class="mask-action-btn" title="重新选择文件替换">
                      <el-icon :size="16">
                        <Refresh />
                      </el-icon>
                      <span>重新上传</span>
                    </span>
                  </el-upload>
                  <span class="mask-action-btn danger-btn" title="删除图片" @click="handleRemovePano">
                    <el-icon :size="16">
                      <Delete />
                    </el-icon>
                    <span>删除</span>
                  </span>
                </div>
              </div>
              <div class="card-status-badge">
                <el-icon>
                  <CircleCheckFilled />
                </el-icon>
                <span>已直传 R2</span>
              </div>
            </div>

            <!-- 未上传状态：Element 拖拽/点击上传卡片 -->
            <el-upload v-else class="pano-uploader-dropzone" drag :show-file-list="false"
              :http-request="handlePanoramaUpload" accept="image/*" :disabled="uploadingPano">
              <div v-loading="uploadingPano" element-loading-text="正在上传..." class="dropzone-inner">
                <template v-if="!uploadingPano">
                  <el-icon class="dropzone-icon">
                    <UploadFilled />
                  </el-icon>
                  <div class="dropzone-text">
                    点击或拖拽上传 <em>360 全景原图</em>
                  </div>
                  <div class="dropzone-tip">
                    建议尺寸：4096×2048 或 8192×4096 球形等距贴图 (2:1)，支持最大 100MB
                  </div>
                </template>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <!-- ⚡ 渐进式秒开低清全景底图 (LQIP: 1024×512, 约 30KB) -->
        <el-form-item label="秒开低清底图" prop="lowResUrl">
          <div class="custom-upload-wrapper">
            <!-- 已就绪状态 -->
            <div
              v-if="form.lowResUrl"
              class="upload-image-card low-card"
              v-loading="uploadingLowRes || compressingLowRes"
              element-loading-text="正在处理..."
              element-loading-background="rgba(255, 255, 255, 0.85)"
            >
              <el-image :src="form.lowResUrl" fit="cover" class="card-img" :preview-src-list="[form.lowResUrl]"
                preview-teleported />
              <div class="card-mask">
                <div class="mask-action-list">
                  <span class="mask-action-btn" title="查看低清底图" @click="openImagePreview(form.lowResUrl)">
                    <el-icon :size="16"><ZoomIn /></el-icon>
                    <span>预览</span>
                  </span>
                  <el-upload
                    class="reupload-trigger"
                    :show-file-list="false"
                    :http-request="handleLowResUpload"
                    :disabled="uploadingLowRes"
                    accept="image/*"
                  >
                    <span class="mask-action-btn" title="手动上传替换">
                      <el-icon :size="16"><Refresh /></el-icon>
                      <span>替换</span>
                    </span>
                  </el-upload>
                  <span class="mask-action-btn danger-btn" title="移除低清图" @click="form.lowResUrl = ''">
                    <el-icon :size="16"><Delete /></el-icon>
                    <span>删除</span>
                  </span>
                </div>
              </div>
              <div class="card-status-badge lqip-status-badge">
                <el-icon><Lightning /></el-icon>
                <span>秒开底图已就绪 (~30KB)</span>
              </div>
            </div>

            <!-- 未生成低清图状态 -->
            <div v-else class="lqip-empty-card" v-loading="compressingLowRes" element-loading-text="正在生成低清底图...">
              <div class="lqip-empty-content">
                <el-icon class="lqip-icon"><Lightning /></el-icon>
                <div class="lqip-tip-info">
                  <span class="lqip-title">渐进式秒开全景底图 (LQIP, ~30KB)</span>
                  <span class="lqip-desc">上传原图时浏览器会自动本地压缩生成；进入全景时 0.05s 极速秒开消除等待。</span>
                </div>
                <div class="lqip-actions">
                  <el-button
                    v-if="form.panoramaUrl"
                    type="primary"
                    size="small"
                    icon="MagicStick"
                    :loading="compressingLowRes"
                    @click="generateLowResFromCurrentPano"
                  >
                    从原图一键自动提取
                  </el-button>
                  <el-upload
                    class="manual-lqip-upload"
                    :show-file-list="false"
                    :http-request="handleLowResUpload"
                    :disabled="uploadingLowRes"
                    accept="image/*"
                  >
                    <el-button size="small" icon="Upload">手动上传</el-button>
                  </el-upload>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 场景缩略图上传 (Element 规范卡片，隐藏原始 URL) -->
        <el-form-item label="场景缩略图" prop="previewUrl">
          <div class="custom-upload-wrapper">
            <!-- 已上传状态 -->
            <div
              v-if="form.previewUrl"
              class="upload-image-card thumb-card"
              v-loading="uploadingPreview"
              element-loading-text="正在上传..."
              element-loading-background="rgba(255, 255, 255, 0.85)"
            >
              <el-image :src="form.previewUrl" fit="cover" class="card-img" :preview-src-list="[form.previewUrl]"
                preview-teleported />
              <div class="card-mask">
                <div class="mask-action-list">
                  <span class="mask-action-btn" title="查看缩略图" @click="openImagePreview(form.previewUrl)">
                    <el-icon :size="16">
                      <ZoomIn />
                    </el-icon>
                    <span>预览</span>
                  </span>
                  <el-upload
                    class="reupload-trigger"
                    :show-file-list="false"
                    :http-request="handlePreviewUpload"
                    :disabled="uploadingPreview"
                    accept="image/*"
                  >
                    <span class="mask-action-btn" title="重新选择文件替换">
                      <el-icon :size="16">
                        <Refresh />
                      </el-icon>
                      <span>重新上传</span>
                    </span>
                  </el-upload>
                  <span class="mask-action-btn danger-btn" title="删除缩略图" @click="form.previewUrl = ''">
                    <el-icon :size="16">
                      <Delete />
                    </el-icon>
                    <span>删除</span>
                  </span>
                </div>
              </div>
              <div class="card-status-badge">
                <el-icon>
                  <CircleCheckFilled />
                </el-icon>
                <span>缩略图已就绪</span>
              </div>
            </div>

            <!-- 未上传状态 -->
            <el-upload v-else class="thumb-uploader-dropzone" drag :show-file-list="false"
              :http-request="handlePreviewUpload" accept="image/*" :disabled="uploadingPreview">
              <div v-loading="uploadingPreview" element-loading-text="正在上传..." class="dropzone-inner thumb-inner">
                <template v-if="!uploadingPreview">
                  <el-icon class="dropzone-icon">
                    <PictureFilled />
                  </el-icon>
                  <div class="dropzone-text">
                    点击或拖拽上传 <em>场景缩略图</em>
                  </div>
                  <div class="dropzone-tip">
                    可选。用于底部场景抽屉（若未上传则默认使用全景原图微缩）
                  </div>
                </template>
              </div>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 地图打点坐标 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="打点 X 轴 (%)" prop="leftPercent">
              <el-input-number v-model="form.leftPercent" :min="0" :max="100" :precision="2" :step="0.5"
                controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="打点 Y 轴 (%)" prop="topPercent">
              <el-input-number v-model="form.topPercent" :min="0" :max="100" :precision="2" :step="0.5"
                controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="初始航向角" prop="initialDeg">
              <el-input-number v-model="form.initialDeg" :min="-180" :max="180" controls-position="right"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序权重">
              <el-input-number v-model="form.sort" :min="0" :max="9999" controls-position="right"
                style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 🌟 场景专属脚底补地遮罩 (默认继承园区配置，可场景个别覆盖定制) -->
        <NadirConfigPanel
          v-model="form.nadirConfig"
          :is-scene-level="true"
          title="场景独立脚底补地遮罩 (Nadir Patch)"
        />

        <el-form-item label="启用状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存更新' : '立即创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 大图放大查看器 (支持点开全屏看大图、缩放、旋转) -->
    <el-image-viewer v-if="isViewerOpen" :url-list="[previewViewerUrl]" @close="isViewerOpen = false" />

    <!-- 🌟 瓦片切片进度与失败诊断看板弹窗 -->
    <el-dialog
      v-model="tileDialogVisible"
      :title="tileDialogTitle"
      width="580px"
      append-to-body
      destroy-on-close
    >
      <div v-loading="tileDialogLoading" class="tile-diag-container">
        <!-- 头部场景简要信息 -->
        <div class="tile-diag-header">
          <div class="diag-scene-title">
            <el-icon class="mr-1"><PictureFilled /></el-icon>
            <span>场景：<strong>{{ currentDiagScene?.name }}</strong></span>
          </div>
          <el-tag :type="getTileTagType(currentDiagProgress?.status)" size="small">
            {{ getTileStatusText(currentDiagProgress?.status) }}
          </el-tag>
        </div>

        <!-- 进度条区域 -->
        <div class="tile-diag-progress-box">
          <div class="progress-labels">
            <span class="step-desc">{{ currentDiagProgress?.step || '准备切片中...' }}</span>
            <span class="percent-num">{{ currentDiagProgress?.percent || 0 }}%</span>
          </div>
          <el-progress
            :percentage="currentDiagProgress?.percent || 0"
            :status="currentDiagProgress?.status === 3 ? 'exception' : (currentDiagProgress?.status === 2 ? 'success' : '')"
            :stroke-width="12"
            :striped="currentDiagProgress?.status === 1"
            :striped-flow="currentDiagProgress?.status === 1"
          />
        </div>

        <!-- 详细数据指标网格 -->
        <div class="tile-diag-stats-grid">
          <div class="diag-stat-item">
            <span class="stat-k">瓦片上传进度</span>
            <span class="stat-v">
              <strong>{{ currentDiagProgress?.uploadedTiles || 0 }}</strong>
              <span class="stat-slash">/</span>
              {{ currentDiagProgress?.totalTiles || '-' }} 块
            </span>
          </div>
          <div class="diag-stat-item">
            <span class="stat-k">当前处理面</span>
            <span class="stat-v">{{ currentDiagProgress?.currentFace ? formatFaceName(currentDiagProgress?.currentFace) : '等距柱状原图' }}</span>
          </div>
          <div class="diag-stat-item">
            <span class="stat-k">累计耗时</span>
            <span class="stat-v">{{ currentDiagProgress?.costSeconds || 0 }} 秒</span>
          </div>
          <div class="diag-stat-item">
            <span class="stat-k">更新时间</span>
            <span class="stat-v">{{ formatTime(currentDiagProgress?.updateTime) }}</span>
          </div>
        </div>

        <!-- 如果失败：红色显眼警示卡片与排查建议 -->
        <div v-if="currentDiagProgress?.status === 3" class="tile-diag-error-box">
          <div class="error-box-title">
            <el-icon><WarningFilled /></el-icon>
            <span>切片中止 / 异常原因诊断：</span>
          </div>
          <div class="error-msg-content">
            <code>{{ currentDiagProgress?.errorMessage || '未知异常（请检查后端服务器日志或网络连接）' }}</code>
          </div>
          <div class="error-tips">
            <div class="tips-title">💡 常见排查与处理建议：</div>
            <ul>
              <li><strong>原图网络访问受限</strong>：请检查 Cloudflare R2 原图公网访问是否正常，防盗链配置是否拦截了后端服务器 IP；</li>
              <li><strong>原图非标准球形等距柱状图</strong>：VR 切片算法要求图片为 2:1 等距全景展开图（推荐 4096×2048 或 8192×4096）；</li>
              <li><strong>网络波动或超时</strong>：可尝试点击下方【重新切片】再次触发后台流水线。</li>
            </ul>
          </div>
        </div>

        <!-- 如果已就绪：成功信息与前缀 -->
        <div v-else-if="currentDiagProgress?.status === 2" class="tile-diag-success-box">
          <el-alert
            type="success"
            :closable="false"
            show-icon
            title="瓦片网格已就绪"
            :description="`瓦片已全部分发至 Cloudflare R2，访客进入该场景将享受毫秒级自适应高清瓦片加载！路径：${currentDiagProgress?.tilePrefix || currentDiagScene?.tilePrefix || '-'}`"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="tileDialogVisible = false">关闭</el-button>
          <el-button
            v-if="currentDiagProgress?.status === 3 || currentDiagProgress?.status === 2"
            type="primary"
            icon="Refresh"
            :loading="currentDiagScene?._generatingTiles"
            @click="handleRetryFromDialog"
          >
            {{ currentDiagProgress?.status === 3 ? '重新切片' : '重新生成瓦片' }}
          </el-button>
          <el-button
            v-else-if="currentDiagProgress?.status === 1"
            type="primary"
            icon="Refresh"
            @click="refreshTileProgress(currentDiagScene?.id)"
          >
            刷新进度
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { vrApi } from '@/api/vr'
import NadirConfigPanel from './components/NadirConfigPanel.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const sceneList = ref([])
const categoryOptions = ref([])

// 大图全屏查看器状态
const isViewerOpen = ref(false)
const previewViewerUrl = ref('')

function openImagePreview(url) {
  if (!url) return
  previewViewerUrl.value = url
  isViewerOpen.value = true
}

function handleRemovePano() {
  form.panoramaUrl = ''
  formRef.value?.validateField('panoramaUrl')
}

// 搜索条件
const queryForm = reactive({
  categoryId: undefined,
  name: '',
  status: undefined
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const uploadingPano = ref(false)
const uploadingPreview = ref(false)
const uploadingLowRes = ref(false)
const compressingLowRes = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  categoryId: null,
  name: '',
  panoramaUrl: '',
  lowResUrl: '',
  previewUrl: '',
  leftPercent: 0,
  topPercent: 0,
  initialDeg: 0,
  nadirConfig: '',
  sort: 1,
  status: 1
})

const formRules = {
  categoryId: [{ required: true, message: '请选择所属园区分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入场景名称', trigger: 'blur' }],
  panoramaUrl: [{ required: true, message: '请上传 360 全景原图', trigger: 'change' }]
}

// 获取分类下拉列表
async function fetchCategories() {
  try {
    const res = await vrApi.categoryList()
    if (res.data) {
      categoryOptions.value = res.data
    }
  } catch (err) { }
}

// ==========================================
// 瓦片切片进度与失败诊断弹窗控制
// ==========================================
const tileDialogVisible = ref(false)
const tileDialogLoading = ref(false)
const currentDiagScene = ref(null)
const currentDiagProgress = ref(null)

const tileDialogTitle = computed(() => {
  if (!currentDiagScene.value) return '瓦片切片监控'
  return `瓦片切片监控 - ${currentDiagScene.value.name}`
})

async function openTileProgressDialog(row) {
  currentDiagScene.value = row
  tileDialogVisible.value = true
  await refreshTileProgress(row.id)
}

async function refreshTileProgress(sceneId) {
  if (!sceneId) return
  tileDialogLoading.value = true
  try {
    const res = await vrApi.tileProgress(sceneId)
    if (res && res.data) {
      currentDiagProgress.value = res.data
      if (currentDiagScene.value) {
        currentDiagScene.value._progress = res.data
        if (res.data.status !== undefined) {
          currentDiagScene.value.tileStatus = res.data.status
        }
      }
    }
  } catch (err) {
    ElMessage.error('获取瓦片进度详情失败')
  } finally {
    tileDialogLoading.value = false
  }
}

async function handleRetryFromDialog() {
  if (!currentDiagScene.value) return
  tileDialogVisible.value = false
  await handleGenerateTiles(currentDiagScene.value)
}

function formatFaceName(face) {
  const map = {
    f: '前 (Front)',
    b: '后 (Back)',
    l: '左 (Left)',
    r: '右 (Right)',
    u: '上 (Top)',
    d: '下 (Bottom)'
  }
  return map[face] || face
}

function getTileTagType(status) {
  switch (status) {
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'danger'
    default: return 'info'
  }
}

function getTileStatusText(status) {
  switch (status) {
    case 1: return '切片处理中'
    case 2: return '已成功就绪'
    case 3: return '切片失败/中止'
    default: return '未切片'
  }
}

function formatTime(timestamp) {
  if (!timestamp) return '-'
  const d = new Date(timestamp)
  return d.toLocaleTimeString()
}

// ==========================================
// 实时轮询机制：列表同步 + 切片进度平滑更新
// ==========================================
let pollTimer = null

async function pollProgressForProcessingScenes() {
  const processingItems = sceneList.value.filter(item => item.tileStatus === 1)
  if (processingItems.length === 0) return

  await Promise.all(
    processingItems.map(async item => {
      try {
        const pRes = await vrApi.tileProgress(item.id)
        if (pRes && pRes.data) {
          item._progress = pRes.data
          if (pRes.data.status !== undefined && pRes.data.status !== 1) {
            item.tileStatus = pRes.data.status
          }
          if (currentDiagScene.value && currentDiagScene.value.id === item.id) {
            currentDiagProgress.value = pRes.data
          }
        }
      } catch (e) {}
    })
  )
}

function checkAndStartPolling() {
  const hasProcessing = sceneList.value.some(item => item.tileStatus === 1)
  if (hasProcessing && !pollTimer) {
    pollProgressForProcessingScenes()
    pollTimer = setInterval(async () => {
      try {
        const res = await vrApi.sceneList(queryForm)
        if (res.data) {
          const progressMap = {}
          sceneList.value.forEach(item => {
            if (item._progress) progressMap[item.id] = item._progress
          })

          sceneList.value = res.data.map(newItem => {
            if (progressMap[newItem.id]) {
              newItem._progress = progressMap[newItem.id]
            }
            return newItem
          })

          await pollProgressForProcessingScenes()

          const stillProcessing = sceneList.value.some(item => item.tileStatus === 1)
          if (!stillProcessing) {
            stopPolling()
          }
        }
      } catch (e) {
        stopPolling()
      }
    }, 2000)
  } else if (!hasProcessing && pollTimer) {
    stopPolling()
  }
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 获取场景列表
async function fetchScenes(silent = false) {
  if (!silent) loading.value = true
  try {
    const res = await vrApi.sceneList(queryForm)
    if (res.data) {
      sceneList.value = res.data
      checkAndStartPolling()
      // 预拉取失败或进行中场景的进度信息
      sceneList.value.forEach(item => {
        if (item.tileStatus === 3 || item.tileStatus === 1) {
          vrApi.tileProgress(item.id).then(pRes => {
            if (pRes && pRes.data) {
              item._progress = pRes.data
            }
          }).catch(() => {})
        }
      })
    }
  } catch (err) {
  } finally {
    if (!silent) loading.value = false
  }
}

function handleQuery() {
  fetchScenes()
}

function handleReset() {
  queryForm.categoryId = undefined
  queryForm.name = ''
  queryForm.status = undefined
  fetchScenes()
}

// 打开新增
function openAddDialog() {
  isEdit.value = false
  form.id = null
  form.categoryId = queryForm.categoryId || (categoryOptions.value[0]?.id ?? null)
  form.name = ''
  form.panoramaUrl = ''
  form.lowResUrl = ''
  form.previewUrl = ''
  form.leftPercent = 50.00
  form.topPercent = 50.00
  form.initialDeg = 0
  form.nadirConfig = '' // 默认继承园区配置
  form.sort = (sceneList.value.length + 1) * 10
  form.status = 1
  dialogVisible.value = true
}

// 打开编辑
function openEditDialog(row) {
  isEdit.value = true
  form.id = row.id
  form.categoryId = row.categoryId
  form.name = row.name
  form.panoramaUrl = row.panoramaUrl
  form.lowResUrl = row.lowResUrl || ''
  form.previewUrl = row.previewUrl || ''
  form.leftPercent = Number(row.leftPercent) || 0
  form.topPercent = Number(row.topPercent) || 0
  form.initialDeg = row.initialDeg || 0
  form.nadirConfig = row.nadirConfig || ''
  form.sort = row.sort || 0
  form.status = row.status !== undefined ? row.status : 1
  dialogVisible.value = true
}

/**
 * 客户端 Canvas 高性能压缩生成全景超轻低清秒开图 (1024x512, 质量 0.35, ~30KB)
 * 纯客户端本地计算，支持传入 File 对象或线上图片 URL
 */
function compressToLowResPano(source, originalName = 'panorama.webp') {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'

    img.onload = () => {
      try {
        const canvas = document.createElement('canvas')
        canvas.width = 1024
        canvas.height = 512
        const ctx = canvas.getContext('2d')
        ctx.imageSmoothingEnabled = true
        ctx.imageSmoothingQuality = 'medium'
        ctx.drawImage(img, 0, 0, 1024, 512)

        canvas.toBlob((blob) => {
          if (!blob) {
            // 若不支持 webp 则降级为 jpeg
            canvas.toBlob((jpegBlob) => {
              if (!jpegBlob) return reject(new Error('Canvas 导出低清底图失败'))
              const fileName = 'low_' + originalName.replace(/\.[^/.]+$/, '') + '.jpg'
              resolve(new File([jpegBlob], fileName, { type: 'image/jpeg' }))
            }, 'image/jpeg', 0.4)
            return
          }
          const fileName = 'low_' + originalName.replace(/\.[^/.]+$/, '') + '.webp'
          resolve(new File([blob], fileName, { type: 'image/webp' }))
        }, 'image/webp', 0.35)
      } catch (err) {
        reject(err)
      }
    }

    img.onerror = () => reject(new Error('图片载入失败，无法生成低清图'))

    if (source instanceof File || source instanceof Blob) {
      const reader = new FileReader()
      reader.onload = (e) => {
        img.src = e.target.result
      }
      reader.onerror = () => reject(new Error('读取文件失败'))
      reader.readAsDataURL(source)
    } else if (typeof source === 'string') {
      img.src = source
    } else {
      reject(new Error('不支持的图片来源'))
    }
  })
}

// 上传全景图直传 R2（并自动客户端压制生成低清秒开底图）
async function handlePanoramaUpload(options) {
  if (uploadingPano.value) return
  const file = options.file
  uploadingPano.value = true
  compressingLowRes.value = true
  try {
    // 1. 客户端离屏 Canvas 本地并发压制 1024x512 超轻低清图 (约 30KB)
    let lowResUploadTask = null
    try {
      const lowResFile = await compressToLowResPano(file, file.name)
      lowResUploadTask = vrApi.uploadImage(lowResFile, 'vr_panorama_low')
    } catch (compressErr) {
      console.warn('本地低清底图自动压制跳过:', compressErr)
    }

    // 2. 原图直传 Cloudflare R2
    const panoUploadTask = vrApi.uploadImage(file, 'vr_panorama')

    const [panoRes, lowResResult] = await Promise.allSettled([
      panoUploadTask,
      lowResUploadTask
    ])

    if (panoRes.status === 'fulfilled' && panoRes.value?.data) {
      form.panoramaUrl = panoRes.value.data
      formRef.value?.clearValidate('panoramaUrl')
      ElMessage.success('360 全景原图已成功上传到 Cloudflare R2！')
    } else {
      throw (panoRes.reason || new Error('全景原图上传失败'))
    }

    if (lowResResult && lowResResult.status === 'fulfilled' && lowResResult.value?.data) {
      form.lowResUrl = lowResResult.value.data
      ElMessage.success('⚡ 渐进式秒开低清底图(~30KB)已自动生成并就绪！')
    }
  } catch (err) {
    console.error('全景原图上传失败:', err)
    ElMessage.error(err?.response?.data?.message || err?.message || '全景原图上传失败，请重试')
  } finally {
    uploadingPano.value = false
    compressingLowRes.value = false
  }
}

// 手动上传低清秒开图直传 R2
async function handleLowResUpload(options) {
  if (uploadingLowRes.value) return
  const file = options.file
  uploadingLowRes.value = true
  try {
    const res = await vrApi.uploadImage(file, 'vr_panorama_low')
    if (res.data) {
      form.lowResUrl = res.data
      ElMessage.success('低清秒开底图已成功上传！')
    }
  } catch (err) {
    console.error('低清底图上传失败:', err)
    ElMessage.error(err?.response?.data?.message || err?.message || '低清底图上传失败，请重试')
  } finally {
    uploadingLowRes.value = false
  }
}

// 从当前已有的全景原图一键自动提取生成低清底图
async function generateLowResFromCurrentPano() {
  if (!form.panoramaUrl) return
  compressingLowRes.value = true
  try {
    const lowResFile = await compressToLowResPano(form.panoramaUrl, 'scene_pano.webp')
    const res = await vrApi.uploadImage(lowResFile, 'vr_panorama_low')
    if (res.data) {
      form.lowResUrl = res.data
      ElMessage.success('⚡ 已成功从原图提取生成渐进式秒开底图！')
    }
  } catch (err) {
    console.error('提取低清底图失败:', err)
    ElMessage.error('从原图提取失败，请检查原图是否可跨域访问，或手动上传')
  } finally {
    compressingLowRes.value = false
  }
}

// 上传预览缩略图直传 R2
async function handlePreviewUpload(options) {
  if (uploadingPreview.value) return
  const file = options.file
  uploadingPreview.value = true
  try {
    const res = await vrApi.uploadImage(file, 'vr_preview')
    if (res.data) {
      form.previewUrl = res.data
      ElMessage.success('场景缩略图已成功上传到 Cloudflare R2！')
    }
  } catch (err) {
    console.error('缩略图上传失败:', err)
    ElMessage.error(err?.response?.data?.message || err?.message || '缩略图上传失败，请重试')
  } finally {
    uploadingPreview.value = false
  }
}

// 提交表单
function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await vrApi.updateScene(form.id, { ...form })
        ElMessage.success('场景修改成功')
      } else {
        await vrApi.addScene({ ...form })
        ElMessage.success('场景点位创建成功')
      }
      dialogVisible.value = false
      fetchScenes()
    } catch (err) {
    } finally {
      submitting.value = false
    }
  })
}

// 删除场景
async function handleDelete(id) {
  try {
    await vrApi.deleteScene(id)
    ElMessage.success('场景点位已删除')
    fetchScenes()
  } catch (err) { }
}

// 跳转到可视化打点编辑器
function goToMapEditor(categoryId, sceneId) {
  router.push({
    path: '/admin/vr/editor',
    query: {
      categoryId: categoryId || queryForm.categoryId,
      sceneId: sceneId || undefined
    }
  })
}

// 手动触发生成瓦片切片
async function handleGenerateTiles(row) {
  row._generatingTiles = true
  try {
    await vrApi.generateTiles(row.id)
    ElMessage.success(`场景【${row.name}】瓦片切片任务已提交后台处理！`)
    row.tileStatus = 1
    row._progress = {
      status: 1,
      percent: 5,
      step: '切片任务已提交线程池排队...',
      uploadedTiles: 0,
      totalTiles: 0,
      costSeconds: 0
    }
    checkAndStartPolling()
  } catch (err) {
    console.error('提交切片任务失败:', err)
  } finally {
    row._generatingTiles = false
  }
}

onMounted(async () => {
  await fetchCategories()
  // 支持路由传入初始 categoryId (例如从分类列表点进来)
  if (route.query.categoryId) {
    queryForm.categoryId = String(route.query.categoryId)
  }
  fetchScenes()
})

onBeforeUnmount(() => {
  stopPolling()
})
</script>

<style scoped>
.vr-scene-manage-page {
  padding-bottom: 24px;
}

.table-card {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 20px;
}

.query-form {
  margin-bottom: -18px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.thumb-container {
  width: 56px;
  height: 42px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-img {
  width: 100%;
  height: 100%;
}

.no-thumb {
  font-size: 11px;
  color: #94a3b8;
}

.scene-info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cat-tag {
  align-self: flex-start;
}

.panorama-link {
  color: #0284c7;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.panorama-link:hover {
  text-decoration: underline;
}

.coord-tag-group {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.pano-col-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.url-text {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lqip-badge-wrap {
  display: flex;
  align-items: center;
}

.lqip-tag {
  font-size: 11px;
}

.custom-upload-wrapper {
  width: 100%;
}

.upload-image-card {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #cbd5e1;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  background: #0f172a;
  transition: all 0.3s ease;
}

.pano-card {
  height: 160px;
}

.low-card {
  height: 140px;
}

.thumb-card {
  height: 110px;
}

.lqip-status-badge {
  background: rgba(14, 165, 233, 0.92) !important;
}

.lqip-empty-card {
  width: 100%;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.lqip-empty-card:hover {
  border-color: #38bdf8;
  background: #f0f9ff;
}

.lqip-empty-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.lqip-icon {
  font-size: 26px;
  color: #0284c7;
}

.lqip-tip-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.lqip-title {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.lqip-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.lqip-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-img {
  width: 100%;
  height: 100%;
  display: block;
  cursor: pointer;
}

.card-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.72);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s ease;
}

.upload-image-card:hover .card-mask {
  opacity: 1;
}

.mask-action-list {
  display: flex;
  align-items: center;
  gap: 14px;
}

.mask-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #f8fafc;
  font-size: 12px;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.16);
  transition: all 0.2s ease;
  user-select: none;
}

.mask-action-btn:hover {
  background: rgba(255, 255, 255, 0.32);
  transform: translateY(-2px);
}

.mask-action-btn.danger-btn:hover {
  background: rgba(239, 68, 68, 0.85);
  color: #fff;
}

.reupload-trigger :deep(.el-upload) {
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(16, 185, 129, 0.9);
  color: #fff;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 12px;
  pointer-events: none;
}

.pano-uploader-dropzone,
.thumb-uploader-dropzone {
  width: 100%;
}

.pano-uploader-dropzone :deep(.el-upload),
.thumb-uploader-dropzone :deep(.el-upload) {
  width: 100%;
  display: block;
}

.pano-uploader-dropzone :deep(.el-upload-dragger),
.thumb-uploader-dropzone :deep(.el-upload-dragger) {
  width: 100%;
  padding: 20px 14px;
  border: 2px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
  transition: all 0.2s ease;
}

.thumb-uploader-dropzone :deep(.el-upload-dragger) {
  padding: 14px;
}

.pano-uploader-dropzone :deep(.el-upload-dragger:hover),
.thumb-uploader-dropzone :deep(.el-upload-dragger:hover) {
  border-color: #3b82f6;
  background: #eff6ff;
}

.dropzone-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.dropzone-icon {
  font-size: 36px;
  color: #3b82f6;
  margin-bottom: 6px;
}

.dropzone-text {
  font-size: 13px;
  color: #334155;
  margin-bottom: 4px;
}

.dropzone-text em {
  color: #3b82f6;
  font-style: normal;
  font-weight: 600;
}

.dropzone-tip {
  font-size: 12px;
  color: #94a3b8;
}

/* ==========================================
   瓦片切片进度与诊断看板样式
   ========================================== */
.clickable-tag {
  cursor: pointer;
  transition: all 0.2s ease;
}

.clickable-tag:hover {
  transform: translateY(-1px);
  filter: brightness(0.95);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.tiling-progress-pill {
  display: flex;
  flex-direction: column;
  gap: 3px;
  background: #f0f7ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  padding: 4px 8px;
  cursor: pointer;
  min-width: 140px;
  max-width: 190px;
  transition: all 0.2s ease;
  user-select: none;
}

.tiling-progress-pill:hover {
  background: #e0f2fe;
  border-color: #38bdf8;
  box-shadow: 0 2px 6px rgba(56, 189, 248, 0.2);
  transform: translateY(-1px);
}

.pill-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
}

.pill-label {
  font-weight: 600;
  color: #0284c7;
  display: flex;
  align-items: center;
  gap: 3px;
}

.pill-percent {
  font-weight: 700;
  color: #0369a1;
  font-family: monospace;
}

.pill-progress-bar {
  margin: 1px 0;
}

.pill-step-text {
  font-size: 10px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 170px;
}

/* 弹窗内部样式 */
.tile-diag-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0;
}

.tile-diag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 10px 14px;
  border-radius: 6px;
}

.diag-scene-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #1e293b;
}

.tile-diag-progress-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 14px;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.step-desc {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.percent-num {
  font-size: 16px;
  font-weight: 700;
  color: #3b82f6;
  font-family: monospace;
}

.tile-diag-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.diag-stat-item {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-k {
  font-size: 11px;
  color: #64748b;
}

.stat-v {
  font-size: 13px;
  color: #1e293b;
  font-weight: 500;
}

.stat-v strong {
  color: #3b82f6;
  font-size: 15px;
}

.stat-slash {
  margin: 0 3px;
  color: #94a3b8;
}

.tile-diag-error-box {
  background: #fff5f5;
  border: 1px solid #fecaca;
  border-radius: 6px;
  padding: 12px 14px;
}

.error-box-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 8px;
}

.error-msg-content {
  background: #fef2f2;
  border: 1px dashed #f87171;
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 10px;
}

.error-msg-content code {
  color: #b91c1c;
  font-size: 12px;
  word-break: break-all;
  white-space: pre-wrap;
  font-family: monospace;
}

.error-tips {
  font-size: 12px;
  color: #7f1d1d;
  line-height: 1.6;
}

.tips-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.error-tips ul {
  margin: 0;
  padding-left: 18px;
}

.tile-diag-success-box {
  margin-top: 4px;
}
</style>
