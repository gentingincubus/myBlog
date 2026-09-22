package org.example.backend.dto.vo;

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
 * 站点导航视图展示对象（VO / View Object）
 * 专供前端展示，承载跨表组装（创建人名称）与派生计算（展示排序值 = 原始排序 * 10）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteNavVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID（雪花算法，String 序列化输出防前端丢精度） */
    private Long id;

    /** 菜单名称 */
    private String name;

    /** 跳转路径 */
    private String path;

    /** 图标或 Emoji */
    private String icon;

    /** 原始排序值（数据库存的 1, 2, 3...） */
    private Integer sort;

    /**
     * 💡【需求字段 1】：内存计算派生字段
     * 展示排序权重 = 原始排序 * 10
     */
    private Integer displaySort;

    /** 是否新窗口打开 */
    private Integer isBlank;

    /** 创建人 ID */
    private Long createBy;

    /**
     * 💡【需求字段 2】：跨表内存组装字段
     * 创建人的昵称与用户名（如："测试小王 (testuser)" 或 "系统内置"）
     */
    private String creatorName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
