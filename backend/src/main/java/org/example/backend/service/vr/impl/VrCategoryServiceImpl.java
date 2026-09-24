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
import org.example.backend.dto.vr.VrCategoryQueryDto;
import org.example.backend.dto.vr.VrCategoryReqDto;
import org.example.backend.dto.vr.vo.VrCategoryVo;
import org.example.backend.entity.vr.VrCategory;
import org.example.backend.entity.vr.VrScene;
import org.example.backend.mapper.vr.VrCategoryMapper;
import org.example.backend.mapper.vr.VrSceneMapper;
import org.example.backend.service.vr.IVrCategoryService;
import org.example.backend.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * VR 全景分类业务实现类
 */
@Slf4j
@Service
public class VrCategoryServiceImpl extends ServiceImpl<VrCategoryMapper, VrCategory> implements IVrCategoryService {

    @Autowired
    private VrSceneMapper vrSceneMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** VR 分类全量公开列表缓存 Key */
    public static final String CACHE_KEY_VR_CATEGORY_LIST = "vr:category:list:all";

    @Override
    public Long addCategory(VrCategoryReqDto dto) {
        String name = dto.getName().trim();
        String code = dto.getCode().trim().toLowerCase();

        // 1. 唯一性校验：分类名称或英文标识不能重复
        long count = this.count(new LambdaQueryWrapper<VrCategory>()
                .eq(VrCategory::getName, name)
                .or()
                .eq(VrCategory::getCode, code));
        if (count > 0) {
            throw new BizException("分类名称或分类标识编码已存在，请勿重复添加");
        }

        // 2. 构造实体对象
        VrCategory category = VrCategory.builder()
                .name(name)
                .code(code)
                .mapUrl(StrUtil.isNotBlank(dto.getMapUrl()) ? dto.getMapUrl().trim() : "")
                .description(StrUtil.isNotBlank(dto.getDescription()) ? dto.getDescription().trim() : "")
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        this.save(category);

        // 3. 清理缓存
        CacheUtils.deleteAfterCommit(CACHE_KEY_VR_CATEGORY_LIST);
        log.info("✅ 新增 VR 分类成功 (ID: {}, Name: {})", category.getId(), category.getName());
        return category.getId();
    }

    @Override
    public void updateCategory(Long id, VrCategoryReqDto dto) {
        VrCategory category = this.getById(id);
        if (category == null) {
            throw new BizException("待修改的 VR 分类不存在");
        }

        String name = dto.getName().trim();
        String code = dto.getCode().trim().toLowerCase();

        // 查重：排除当前 ID，检查是否有同名或同编码
        long count = this.count(new LambdaQueryWrapper<VrCategory>()
                .ne(VrCategory::getId, id)
                .and(w -> w.eq(VrCategory::getName, name).or().eq(VrCategory::getCode, code)));
        if (count > 0) {
            throw new BizException("分类名称或标识编码已被其它分类占用");
        }

        category.setName(name);
        category.setCode(code);
        category.setMapUrl(StrUtil.isNotBlank(dto.getMapUrl()) ? dto.getMapUrl().trim() : "");
        category.setDescription(StrUtil.isNotBlank(dto.getDescription()) ? dto.getDescription().trim() : "");
        if (dto.getSort() != null) {
            category.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            category.setStatus(dto.getStatus());
        }

        this.updateById(category);

        // 清理缓存
        CacheUtils.deleteAfterCommit(CACHE_KEY_VR_CATEGORY_LIST);
        log.info("✅ 更新 VR 分类成功 (ID: {})", id);
    }

    @Override
    public void deleteCategory(Long id) {
        VrCategory category = this.getById(id);
        if (category == null) {
            throw new BizException("待删除的 VR 分类不存在");
        }

        // 安全校验：分类下是否还有未删除的场景点位
        Long sceneCount = vrSceneMapper.selectCount(new LambdaQueryWrapper<VrScene>()
                .eq(VrScene::getCategoryId, id));
        if (sceneCount != null && sceneCount > 0) {
            throw new BizException(String.format("该分类下仍存在 %d 个 VR 场景点位，请先删除或转移场景后再删除该分类", sceneCount));
        }

        this.removeById(id);

        // 清理缓存
        CacheUtils.deleteAfterCommit(CACHE_KEY_VR_CATEGORY_LIST);
        log.info("✅ 逻辑删除 VR 分类成功 (ID: {})", id);
    }

    @Override
    public List<VrCategoryVo> listCategories(VrCategoryQueryDto queryDto) {
        boolean isAllQuery = queryDto == null ||
                (StrUtil.isBlank(queryDto.getName()) && StrUtil.isBlank(queryDto.getCode()) && queryDto.getStatus() == null);

        // 1. 尝试读 Redis 旁路缓存
        if (isAllQuery) {
            String cachedJson = redisTemplate.opsForValue().get(CACHE_KEY_VR_CATEGORY_LIST);
            if (StrUtil.isNotBlank(cachedJson)) {
                try {
                    log.info("🎯 命中 Redis VR分类列表缓存，极速响应");
                    return objectMapper.readValue(cachedJson, new TypeReference<List<VrCategoryVo>>() {});
                } catch (Exception e) {
                    log.error("VR分类缓存反序列化失败，安全降级查库: {}", e.getMessage());
                }
            }
        }

        // 2. 动态多条件查询
        LambdaQueryWrapper<VrCategory> wrapper = new LambdaQueryWrapper<>();
        if (queryDto != null) {
            wrapper.like(StrUtil.isNotBlank(queryDto.getName()), VrCategory::getName, StrUtil.trim(queryDto.getName()))
                   .like(StrUtil.isNotBlank(queryDto.getCode()), VrCategory::getCode, StrUtil.trim(queryDto.getCode()))
                   .eq(queryDto.getStatus() != null, VrCategory::getStatus, queryDto.getStatus());
        }
        wrapper.orderByAsc(VrCategory::getSort)
               .orderByAsc(VrCategory::getCreateTime);

        List<VrCategory> categoryList = this.list(wrapper);
        if (CollUtil.isEmpty(categoryList)) {
            return Collections.emptyList();
        }

        // 3. 统计各个分类下的场景总数（一次性查询，内存汇总，零 N+1 问题）
        List<Long> categoryIds = categoryList.stream().map(VrCategory::getId).collect(Collectors.toList());
        List<VrScene> allScenes = vrSceneMapper.selectList(new LambdaQueryWrapper<VrScene>()
                .in(VrScene::getCategoryId, categoryIds)
                .select(VrScene::getId, VrScene::getCategoryId));

        Map<Long, Long> sceneCountMap = allScenes.stream()
                .collect(Collectors.groupingBy(VrScene::getCategoryId, Collectors.counting()));

        // 4. 装配 VO
        List<VrCategoryVo> voList = categoryList.stream().map(cat -> {
            VrCategoryVo vo = BeanUtil.copyProperties(cat, VrCategoryVo.class);
            vo.setSceneCount(sceneCountMap.getOrDefault(cat.getId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());

        // 5. 写入缓存（如果是全量查询）
        if (isAllQuery && CollUtil.isNotEmpty(voList)) {
            try {
                String json = objectMapper.writeValueAsString(voList);
                redisTemplate.opsForValue().set(CACHE_KEY_VR_CATEGORY_LIST, json, 30, TimeUnit.MINUTES);
                log.info("📦 VR分类全量列表已写入 Redis 缓存 (Key: {}, TTL: 30分钟)", CACHE_KEY_VR_CATEGORY_LIST);
            } catch (Exception e) {
                log.error("写入 VR分类 Redis 缓存失败: {}", e.getMessage());
            }
        }

        return voList;
    }

    @Override
    public VrCategoryVo getCategoryById(Long id) {
        VrCategory category = this.getById(id);
        if (category == null) {
            throw new BizException("VR 分类不存在");
        }
        VrCategoryVo vo = BeanUtil.copyProperties(category, VrCategoryVo.class);
        Long count = vrSceneMapper.selectCount(new LambdaQueryWrapper<VrScene>()
                .eq(VrScene::getCategoryId, id));
        vo.setSceneCount(count != null ? count.intValue() : 0);
        return vo;
    }
}

