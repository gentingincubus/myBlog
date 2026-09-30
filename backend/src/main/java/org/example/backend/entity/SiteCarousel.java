package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 首页 3D 毛玻璃轮播图实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("site_carousel")
public class SiteCarousel extends BaseEntity {

    /**
     * 轮播标题（如：顺峰山公园 · 中华第一牌坊）
     */
    private String title;

    /**
     * 副标题/简短标语（如：顺德之门，气势磅礴的岭南建筑丰碑）
     */
    private String subtitle;

    /**
     * 封面图片 URL (Cloudflare R2 全球 CDN 直链)
     */
    private String coverUrl;

    /**
     * 卡片翻转后显示的富文本介绍内容 (Markdown 格式)
     */
    private String content;

    /**
     * 排序权重（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（1: 启用, 0: 禁用）
     */
    private Integer status;
}
