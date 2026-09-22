package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 站点导航菜单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("site_nav")
public class SiteNav extends BaseEntity {

    /**
     * 菜单名称（如：首页、个人介绍、花活工坊）
     */
    private String name;

    /**
     * 路由跳转路径（如：/、/about、/tools）
     */
    private String path;

    /**
     * 菜单图标或 Emoji（如：🏠、👤、🧪、🛠️）
     */
    private String icon;

    /**
     * 排序权重（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 是否在新窗口打开（0: 否, 1: 是）
     */
    private Integer isBlank;
}
