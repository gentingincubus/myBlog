package org.example.backend.entity.vr;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.backend.entity.BaseEntity;

/**
 * VR 全景分类与园区实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vr_category")
public class VrCategory extends BaseEntity {

    /**
     * 分类/园区名称（如：顺峰山公园-西区、顺峰山公园-东区、顺峰山龙舟汇）
     */
    private String name;

    /**
     * 英文唯一标识码（如：west_park, east_park, dragon_boat）
     */
    private String code;

    /**
     * 导览俯视底图 URL（可选，为空时前端自动隐藏导览底图）
     */
    private String mapUrl;

    /**
     * 园区/分类简介
     */
    private String description;

    /**
     * 排序权重（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（0: 禁用, 1: 启用）
     */
    private Integer status;
}

