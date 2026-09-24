package org.example.backend.entity.vr;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.backend.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * VR 全景具体场景实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vr_scene")
public class VrScene extends BaseEntity {

    /**
     * 所属分类ID (关联 vr_category.id)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    /**
     * 场景名称（如：伏波桥、龙舟馆入口、牌坊）
     */
    private String name;

    /**
     * 360 全景原图 URL (Cloudflare R2)
     */
    private String panoramaUrl;

    /**
     * 场景缩略图预览 URL (Cloudflare R2，用于底部选择栏和浮标微缩图)
     */
    private String previewUrl;

    /**
     * 导览地图打点 Y 轴百分比坐标 (0.00% - 100.00%)
     */
    private BigDecimal topPercent;

    /**
     * 导览地图打点 X 轴百分比坐标 (0.00% - 100.00%)
     */
    private BigDecimal leftPercent;

    /**
     * 初始进入视角水平航向偏角 (-180° ~ 180°)
     */
    private Integer initialDeg;

    /**
     * 排序权重（数字越小越靠前，用于场景卡片排布）
     */
    private Integer sort;

    /**
     * 状态（0: 禁用, 1: 启用）
     */
    private Integer status;
}

