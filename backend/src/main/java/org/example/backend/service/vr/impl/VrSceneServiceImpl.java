package org.example.backend.service.vr.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.BizException;
import org.example.backend.dto.vr.VrSceneQueryDto;
import org.example.backend.dto.vr.VrSceneReqDto;
import org.example.backend.dto.vr.vo.VrSceneVo;
import org.example.backend.entity.vr.VrCategory;
import org.example.backend.entity.vr.VrScene;
import org.example.backend.mapper.vr.VrCategoryMapper;
import org.example.backend.mapper.vr.VrSceneMapper;
import org.example.backend.service.vr.IVrSceneService;
import org.example.backend.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * VR 全景场景业务实现类
 */
@Slf4j
@Service
public class VrSceneServiceImpl extends ServiceImpl<VrSceneMapper, VrScene> implements IVrSceneService {

    @Autowired
    private VrCategoryMapper vrCategoryMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 场景列表缓存前缀 */
    public static final String CACHE_KEY_VR_SCENE_PREFIX = "vr:scene:list:";

    /** 分类列表缓存 Key（场景变动时需同步更新分类场景数） */
    public static final String CACHE_KEY_VR_CATEGORY_LIST = "vr:category:list:all";

    @Override
    public Long addScene(VrSceneReqDto dto) {
        // 1. 校验所属分类是否存在且未删除
        VrCategory category = vrCategoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BizException("所属的 VR 分类不存在");
        }

        String name = dto.getName().trim();

        // 2. 同一分类下场景名称查重
        long count = this.count(new LambdaQueryWrapper<VrScene>()
                .eq(VrScene::getCategoryId, dto.getCategoryId())
                .eq(VrScene::getName, name));
        if (count > 0) {
            throw new BizException("该分类下已存在同名场景，请勿重复添加");
        }

        // 3. 构建实体并入库
        VrScene scene = VrScene.builder()
                .categoryId(dto.getCategoryId())
                .name(name)
                .panoramaUrl(dto.getPanoramaUrl().trim())
                .previewUrl(StrUtil.isNotBlank(dto.getPreviewUrl()) ? dto.getPreviewUrl().trim() : "")
                .topPercent(dto.getTopPercent() != null ? dto.getTopPercent() : BigDecimal.ZERO)
                .leftPercent(dto.getLeftPercent() != null ? dto.getLeftPercent() : BigDecimal.ZERO)
                .initialDeg(dto.getInitialDeg() != null ? dto.getInitialDeg() : 0)
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        this.save(scene);

        // 4. 清理当前分类的场景缓存 & 全量分类缓存
        cleanSceneCache(scene.getCategoryId());
        log.info("✅ 新增 VR 场景成功 (ID: {}, Name: {}, CategoryId: {})", scene.getId(), scene.getName(), scene.getCategoryId());
        return scene.getId();
    }

    @Override
    public void updateScene(Long id, VrSceneReqDto dto) {
        VrScene scene = this.getById(id);
        if (scene == null) {
            throw new BizException("待修改的 VR 场景不存在");
        }

        Long oldCategoryId = scene.getCategoryId();
        Long newCategoryId = dto.getCategoryId();

        // 若更换了分类，需校验新分类有效性
        if (!Objects.equals(oldCategoryId, newCategoryId)) {
            VrCategory category = vrCategoryMapper.selectById(newCategoryId);
            if (category == null) {
                throw new BizException("所选的新分类不存在");
            }
        }

        String name = dto.getName().trim();
        // 查重：新分类下排除自身后是否存在同名
        long count = this.count(new LambdaQueryWrapper<VrScene>()
                .eq(VrScene::getCategoryId, newCategoryId)
                .eq(VrScene::getName, name)
                .ne(VrScene::getId, id));
        if (count > 0) {
            throw new BizException("目标分类下已存在同名场景");
        }

        scene.setCategoryId(newCategoryId);
        scene.setName(name);
        scene.setPanoramaUrl(dto.getPanoramaUrl().trim());
        scene.setPreviewUrl(StrUtil.isNotBlank(dto.getPreviewUrl()) ? dto.getPreviewUrl().trim() : "");
        if (dto.getTopPercent() != null) {
            scene.setTopPercent(dto.getTopPercent());
        }
        if (dto.getLeftPercent() != null) {
            scene.setLeftPercent(dto.getLeftPercent());
        }
        if (dto.getInitialDeg() != null) {
            scene.setInitialDeg(dto.getInitialDeg());
        }
        if (dto.getSort() != null) {
            scene.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            scene.setStatus(dto.getStatus());
        }

        this.updateById(scene);

        // 清理缓存（若跨分类，需清理两个分类各自的缓存）
        cleanSceneCache(oldCategoryId);
        if (!Objects.equals(oldCategoryId, newCategoryId)) {
            cleanSceneCache(newCategoryId);
        }
        log.info("✅ 更新 VR 场景成功 (ID: {})", id);
    }

    @Override
    public void deleteScene(Long id) {
        VrScene scene = this.getById(id);
        if (scene == null) {
            throw new BizException("待删除的 VR 场景不存在或已被删除");
        }

        Long categoryId = scene.getCategoryId();
        this.removeById(id);

        cleanSceneCache(categoryId);
        log.info("✅ 逻辑删除 VR 场景成功 (ID: {})", id);
    }

    @Override
    public List<VrSceneVo> listScenes(VrSceneQueryDto queryDto) {
        // 如果是指定分类下的全量查询（无模糊搜索），优先使用缓存
        boolean canCache = queryDto != null && queryDto.getCategoryId() != null &&
                StrUtil.isBlank(queryDto.getName()) && queryDto.getStatus() == null;

        String cacheKey = canCache ? (CACHE_KEY_VR_SCENE_PREFIX + queryDto.getCategoryId()) : null;

        if (canCache) {
            String cachedJson = redisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cachedJson)) {
                try {
                    log.info("🎯 命中 Redis VR场景列表缓存 (Key: {})", cacheKey);
                    return objectMapper.readValue(cachedJson, new TypeReference<List<VrSceneVo>>() {});
                } catch (Exception e) {
                    log.error("VR场景缓存反序列化失败，走查库: {}", e.getMessage());
                }
            }
        }

        // 动态 SQL 条件查询
        LambdaQueryWrapper<VrScene> wrapper = new LambdaQueryWrapper<>();
        if (queryDto != null) {
            wrapper.eq(queryDto.getCategoryId() != null, VrScene::getCategoryId, queryDto.getCategoryId())
                   .like(StrUtil.isNotBlank(queryDto.getName()), VrScene::getName, StrUtil.trim(queryDto.getName()))
                   .eq(queryDto.getStatus() != null, VrScene::getStatus, queryDto.getStatus());
        }
        wrapper.orderByAsc(VrScene::getSort)
               .orderByAsc(VrScene::getCreateTime);

        List<VrScene> sceneList = this.list(wrapper);
        if (CollUtil.isEmpty(sceneList)) {
            return Collections.emptyList();
        }

        // 跨表批量装配分类名称
        Set<Long> categoryIds = sceneList.stream().map(VrScene::getCategoryId).collect(Collectors.toSet());
        Map<Long, VrCategory> categoryMap = Collections.emptyMap();
        if (!categoryIds.isEmpty()) {
            List<VrCategory> categories = vrCategoryMapper.selectByIds(categoryIds);
            categoryMap = categories.stream()
                    .collect(Collectors.toMap(VrCategory::getId, Function.identity(), (c1, c2) -> c1));
        }

        final Map<Long, VrCategory> finalCatMap = categoryMap;
        List<VrSceneVo> voList = sceneList.stream().map(scene -> {
            VrSceneVo vo = BeanUtil.copyProperties(scene, VrSceneVo.class);
            VrCategory cat = finalCatMap.get(scene.getCategoryId());
            vo.setCategoryName(cat != null ? cat.getName() : "未知园区");
            return vo;
        }).collect(Collectors.toList());

        // 写入缓存
        if (canCache && CollUtil.isNotEmpty(voList)) {
            try {
                String json = objectMapper.writeValueAsString(voList);
                redisTemplate.opsForValue().set(cacheKey, json, 30, TimeUnit.MINUTES);
                log.info("📦 VR场景列表已写入 Redis 缓存 (Key: {}, TTL: 30分钟)", cacheKey);
            } catch (Exception e) {
                log.error("写入 VR场景 Redis 缓存失败: {}", e.getMessage());
            }
        }

        return voList;
    }

    @Override
    public VrSceneVo getSceneById(Long id) {
        VrScene scene = this.getById(id);
        if (scene == null) {
            throw new BizException("VR 场景不存在");
        }
        VrSceneVo vo = BeanUtil.copyProperties(scene, VrSceneVo.class);
        VrCategory category = vrCategoryMapper.selectById(scene.getCategoryId());
        vo.setCategoryName(category != null ? category.getName() : "未知园区");
        return vo;
    }

    /**
     * 清理场景相关缓存与分类列表缓存
     */
    private void cleanSceneCache(Long categoryId) {
        if (categoryId != null) {
            CacheUtils.deleteAfterCommit(CACHE_KEY_VR_SCENE_PREFIX + categoryId);
        }
        CacheUtils.deleteAfterCommit(CACHE_KEY_VR_CATEGORY_LIST);
    }
}

