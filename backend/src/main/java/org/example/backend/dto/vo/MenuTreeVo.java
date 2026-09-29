package org.example.backend.dto.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 树形菜单与权限 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单/按钮ID (雪花算法序列化为字符串防前端精度丢失) */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 父菜单ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /** 菜单/按钮名称 */
    private String menuName;

    /** 菜单类型 (M: 目录, C: 菜单, F: 按钮操作) */
    private String menuType;

    /** 路由地址 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String perms;

    /** 菜单图标 */
    private String icon;

    /** 显示顺序 */
    private Integer sort;

    /** 是否可见 (1: 显示, 0: 隐藏) */
    private Integer visible;

    /** 菜单状态 (1: 正常, 0: 停用) */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 子节点列表 */
    @Builder.Default
    private List<MenuTreeVo> children = new ArrayList<>();
}

