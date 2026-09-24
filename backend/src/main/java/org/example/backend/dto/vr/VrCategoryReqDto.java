package org.example.backend.dto.vr;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VR 全景分类创建/更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrCategoryReqDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类/园区名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50 个字符")
    private String name;

    /**
     * 英文唯一标识码
     */
    @NotBlank(message = "分类英文标识码不能为空")
    @Size(max = 50, message = "分类英文标识码长度不能超过 50 个字符")
    private String code;

    /**
     * 导览俯视底图 URL (可选)
     */
    @Size(max = 500, message = "底图 URL 长度不能超过 500 个字符")
    private String mapUrl;

    /**
     * 分类简介
     */
    @Size(max = 255, message = "分类简介长度不能超过 255 个字符")
    private String description;

    /**
     * 排序权重 (数字越小越靠前)
     */
    private Integer sort;

    /**
     * 状态 (0: 禁用, 1: 启用)
     */
    private Integer status;
}

