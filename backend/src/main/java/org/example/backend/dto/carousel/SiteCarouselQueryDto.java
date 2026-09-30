package org.example.backend.dto.carousel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页轮播图查询参数 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCarouselQueryDto {

    /**
     * 标题模糊检索（选填）
     */
    private String title;

    /**
     * 状态筛选（1: 启用, 0: 禁用，选填）
     */
    private Integer status;
}
