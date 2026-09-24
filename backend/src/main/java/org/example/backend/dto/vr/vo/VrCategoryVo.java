package org.example.backend.dto.vr.vo;

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
 * VR 全景分类视图对象 (VO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrCategoryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID (雪花算法，String 序列化输出防止前端丢失精度)
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 英文唯一标识码
     */
    private String code;

    /**
     * 导览俯视底图 URL
     */
    private String mapUrl;

    /**
     * 园区/分类简介
     */
    private String description;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 状态 (0: 禁用, 1: 启用)
     */
    private Integer status;

    /**
     * 💡 该分类下包含的场景点位总数（跨表内存聚合统计）
     */
    private Integer sceneCount;

    /**
     * 创建人用户 ID
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

