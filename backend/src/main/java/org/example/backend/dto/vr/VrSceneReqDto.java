package org.example.backend.dto.vr;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * VR 全景场景创建/更新请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrSceneReqDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属分类ID
     */
    @NotNull(message = "所属分类 ID 不能为空")
    private Long categoryId;

    /**
     * 场景名称 (如: 伏波桥、美的体育广场)
     */
    @NotBlank(message = "场景名称不能为空")
    @Size(max = 50, message = "场景名称长度不能超过 50 个字符")
    private String name;

    /**
     * 360 全景原图 URL
     */
    @NotBlank(message = "全景原图 URL 不能为空")
    @Size(max = 500, message = "全景原图 URL 长度不能超过 500 个字符")
    private String panoramaUrl;

    /**
     * 场景缩略图预览 URL
     */
    @Size(max = 500, message = "缩略图 URL 长度不能超过 500 个字符")
    private String previewUrl;

    /**
     * 导览地图打点 Y 轴百分比坐标 (0.00% - 100.00%)
     */
    @DecimalMin(value = "0.00", message = "打点 Y 轴百分比不能小于 0.00%")
    @DecimalMax(value = "100.00", message = "打点 Y 轴百分比不能大于 100.00%")
    private BigDecimal topPercent;

    /**
     * 导览地图打点 X 轴百分比坐标 (0.00% - 100.00%)
     */
    @DecimalMin(value = "0.00", message = "打点 X 轴百分比不能小于 0.00%")
    @DecimalMax(value = "100.00", message = "打点 X 轴百分比不能大于 100.00%")
    private BigDecimal leftPercent;

    /**
     * 初始进入视角水平航向偏角 (-180° ~ 180°)
     */
    @Min(value = -180, message = "初始航向角不能小于 -180 度")
    @Max(value = 180, message = "初始航向角不能大于 180 度")
    private Integer initialDeg;

    /**
     * 排序权重 (数字越小越靠前)
     */
    private Integer sort;

    /**
     * 状态 (0: 禁用, 1: 启用)
     */
    private Integer status;
}

