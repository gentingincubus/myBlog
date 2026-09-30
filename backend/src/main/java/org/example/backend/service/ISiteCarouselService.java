package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.carousel.SiteCarouselQueryDto;
import org.example.backend.dto.carousel.SiteCarouselReqDto;
import org.example.backend.dto.carousel.SiteCarouselVo;
import org.example.backend.entity.SiteCarousel;

import java.util.List;

/**
 * 首页轮播图业务服务接口
 */
public interface ISiteCarouselService extends IService<SiteCarousel> {

    /**
     * 新增轮播图
     *
     * @param dto 入参
     * @return 新生成的雪花算法 ID
     */
    Long addCarousel(SiteCarouselReqDto dto);

    /**
     * 修改轮播图
     *
     * @param id  轮播ID
     * @param dto 入参
     */
    void updateCarousel(Long id, SiteCarouselReqDto dto);

    /**
     * 逻辑删除轮播图
     *
     * @param id 轮播ID
     */
    void deleteCarousel(Long id);

    /**
     * 更新启停状态
     *
     * @param id     轮播ID
     * @param status 状态（1: 启用, 0: 禁用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 前台门户查询已启用的轮播图列表（优先命中 Redis 旁路缓存）
     */
    List<SiteCarouselVo> listPortalCarousels();

    /**
     * 管理后台按条件查询轮播图列表
     *
     * @param queryDto 查询参数
     */
    List<SiteCarouselVo> listAdminCarousels(SiteCarouselQueryDto queryDto);
}
