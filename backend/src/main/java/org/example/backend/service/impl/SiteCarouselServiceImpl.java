package org.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.BizException;
import org.example.backend.dto.carousel.SiteCarouselQueryDto;
import org.example.backend.dto.carousel.SiteCarouselReqDto;
import org.example.backend.dto.carousel.SiteCarouselVo;
import org.example.backend.entity.SiteCarousel;
import org.example.backend.mapper.SiteCarouselMapper;
import org.example.backend.service.ISiteCarouselService;
import org.example.backend.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 首页轮播图业务实现类 (集成 Redis 旁路缓存与防击穿防护)
 */
@Slf4j
@Service
public class SiteCarouselServiceImpl extends ServiceImpl<SiteCarouselMapper, SiteCarousel> implements ISiteCarouselService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 首页前台轮播图缓存 Key */
    public static final String CACHE_KEY_PORTAL_CAROUSEL_LIST = "home:carousel:list:all";

    @Override
    public Long addCarousel(SiteCarouselReqDto dto) {
        String title = dto.getTitle().trim();

        // 1. 标题防重检查
        long count = this.count(new LambdaQueryWrapper<SiteCarousel>()
                .eq(SiteCarousel::getTitle, title));
        if (count > 0) {
            throw new BizException("已存在同名轮播图，请勿重复创建");
        }

        // 2. 构造实体并持久化
        SiteCarousel carousel = SiteCarousel.builder()
                .title(title)
                .subtitle(StrUtil.nullToEmpty(dto.getSubtitle()).trim())
                .coverUrl(dto.getCoverUrl().trim())
                .content(dto.getContent())
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        this.save(carousel);
        log.info("✅ 新增首页轮播图成功 (ID: {}, Title: {})", carousel.getId(), title);

        cleanCache();
        return carousel.getId();
    }

    @Override
    public void updateCarousel(Long id, SiteCarouselReqDto dto) {
        SiteCarousel carousel = this.getById(id);
        if (carousel == null) {
            throw new BizException("待修改的轮播图不存在或已被删除");
        }

        String title = dto.getTitle().trim();
        if (!carousel.getTitle().equals(title)) {
            long count = this.count(new LambdaQueryWrapper<SiteCarousel>()
                    .eq(SiteCarousel::getTitle, title)
                    .ne(SiteCarousel::getId, id));
            if (count > 0) {
                throw new BizException("已存在同名轮播图，无法修改为此标题");
            }
        }

        carousel.setTitle(title);
        carousel.setSubtitle(StrUtil.nullToEmpty(dto.getSubtitle()).trim());
        carousel.setCoverUrl(dto.getCoverUrl().trim());
        carousel.setContent(dto.getContent());
        if (dto.getSort() != null) {
            carousel.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            carousel.setStatus(dto.getStatus());
        }

        this.updateById(carousel);
        log.info("✅ 更新首页轮播图成功 (ID: {})", id);

        cleanCache();
    }

    @Override
    public void deleteCarousel(Long id) {
        SiteCarousel carousel = this.getById(id);
        if (carousel == null) {
            throw new BizException("待删除的轮播图不存在或已被删除");
        }

        this.removeById(id);
        log.info("✅ 逻辑删除首页轮播图成功 (ID: {})", id);

        cleanCache();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SiteCarousel carousel = this.getById(id);
        if (carousel == null) {
            throw new BizException("指定的轮播图不存在");
        }

        carousel.setStatus(status != null && status == 1 ? 1 : 0);
        this.updateById(carousel);
        log.info("✅ 更新首页轮播图状态成功 (ID: {}, Status: {})", id, carousel.getStatus());

        cleanCache();
    }

    @Override
    public List<SiteCarouselVo> listPortalCarousels() {
        // 1. 优先从 Redis 旁路缓存读取
        String cachedJson = redisTemplate.opsForValue().get(CACHE_KEY_PORTAL_CAROUSEL_LIST);
        if (StrUtil.isNotBlank(cachedJson)) {
            try {
                return objectMapper.readValue(cachedJson, new TypeReference<List<SiteCarouselVo>>() {});
            } catch (Exception e) {
                log.error("首页轮播缓存反序列化失败，走查库兜底: {}", e.getMessage());
            }
        }

        // 2. 缓存未命中，查 MySQL 库：仅查启用且未删除项，按排序正序、ID 正序排列
        List<SiteCarousel> list = this.list(new LambdaQueryWrapper<SiteCarousel>()
                .eq(SiteCarousel::getStatus, 1)
                .orderByAsc(SiteCarousel::getSort)
                .orderByAsc(SiteCarousel::getId));

        List<SiteCarouselVo> voList = convertToVoList(list);

        // 3. 回写 Redis 缓存 (30分钟TTL)
        try {
            String jsonStr = objectMapper.writeValueAsString(voList);
            redisTemplate.opsForValue().set(CACHE_KEY_PORTAL_CAROUSEL_LIST, jsonStr, 30, TimeUnit.MINUTES);
            log.info("💾 首页轮播图已回写 Redis 旁路缓存 (TTL: 30分钟, 条数: {})", voList.size());
        } catch (Exception e) {
            log.error("写入首页轮播 Redis 缓存失败: {}", e.getMessage());
        }

        return voList;
    }

    @Override
    public List<SiteCarouselVo> listAdminCarousels(SiteCarouselQueryDto queryDto) {
        LambdaQueryWrapper<SiteCarousel> wrapper = new LambdaQueryWrapper<>();
        if (queryDto != null) {
            if (StrUtil.isNotBlank(queryDto.getTitle())) {
                wrapper.like(SiteCarousel::getTitle, queryDto.getTitle().trim());
            }
            if (queryDto.getStatus() != null) {
                wrapper.eq(SiteCarousel::getStatus, queryDto.getStatus());
            }
        }
        wrapper.orderByAsc(SiteCarousel::getSort)
               .orderByDesc(SiteCarousel::getId);

        List<SiteCarousel> list = this.list(wrapper);
        return convertToVoList(list);
    }

    /**
     * 清理前台轮播缓存 (事务安全)
     */
    private void cleanCache() {
        try {
            CacheUtils.deleteAfterCommit(CACHE_KEY_PORTAL_CAROUSEL_LIST);
            log.info("🧹 已提交删除首页轮播 Redis 缓存 (Key: {})", CACHE_KEY_PORTAL_CAROUSEL_LIST);
        } catch (Exception e) {
            log.error("清除首页轮播 Redis 缓存失败: {}", e.getMessage());
        }
    }

    private List<SiteCarouselVo> convertToVoList(List<SiteCarousel> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(item -> {
            SiteCarouselVo vo = new SiteCarouselVo();
            BeanUtil.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
