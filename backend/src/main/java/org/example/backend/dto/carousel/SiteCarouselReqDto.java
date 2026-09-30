package org.example.backend.dto.carousel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页轮播图新增/修改请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteCarouselReqDto {

    /**
     * 轮播标题
     */
    @NotBlank(message = "轮播标题不能为空")
    @Size(max = 100, message = "标题长度不能超过 100 个字符")
    private String title;

    /**
     * 副标题/标语（选填）
     */
    @Size(max = 200, message = "副标题长度不能超过 200 个字符")
    private String subtitle;

    /**
     * 封面图片 URL (Cloudflare R2 直链)
     */
    @NotBlank(message = "封面图片不能为空")
    @Size(max = 500, message = "封面链接长度不能超过 500 个字符")
    private String coverUrl;

    /**
     * 卡片翻转后显示的富文本介绍内容 (Markdown 格式)
     */
    @NotBlank(message = "富文本介绍内容不能为空")
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
