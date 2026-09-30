package org.example.backend.dto.carousel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 首页轮播图视图展示对象 (VO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCarouselVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播ID (String 序列化输出防前端 JS 丢失 19 位精度)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 轮播标题
     */
    private String title;

    /**
     * 副标题/标语
     */
    private String subtitle;

    /**
     * 封面图片 URL (Cloudflare R2 直链)
     */
    private String coverUrl;

    /**
     * 卡片背面富文本详细介绍 (Markdown 格式)
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
