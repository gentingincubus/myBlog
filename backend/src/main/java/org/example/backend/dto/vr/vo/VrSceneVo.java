package org.example.backend.dto.vr.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VR 全景场景视图对象 (VO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrSceneVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景ID (雪花算法，String 序列化输出防前端丢精度)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属分类ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    /**
     * 所属分类名称（跨表组装，供后台管理表格展示和前端路径面包屑）
     */
    private String categoryName;

    /**
     * 场景名称 (如: 伏波桥、美的体育广场)
     */
    private String name;

    /**
     * 360 全景原图 URL
     */
    private String panoramaUrl;

    /**
     * 场景缩略图预览 URL
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
     * 排序权重 (数字越小越靠前)
     */
    private Integer sort;

    /**
     * 状态 (0: 禁用, 1: 启用)
     */
    private Integer status;

    /**
     * 创建人 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

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

