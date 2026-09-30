package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SiteCarousel;

/**
 * 首页轮播图 Mapper 接口
 */
@Mapper
public interface SiteCarouselMapper extends BaseMapper<SiteCarousel> {
}
