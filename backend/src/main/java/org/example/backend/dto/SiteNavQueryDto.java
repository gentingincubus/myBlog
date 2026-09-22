package org.example.backend.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 导航菜单列表查询入参 DTO
 */
@Data
public class SiteNavQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单名称（支持模糊搜索）
     */
    private String name;

    /**
     * 路由路径（支持模糊搜索）
     */
    private String path;
}
