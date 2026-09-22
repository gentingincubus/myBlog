package org.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站点导航新增/修改请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteNavReqDto {

    /**
     * 菜单名称
     */
    @NotBlank(message = "导航名称不能为空")
    @Size(max = 10, message = "导航名称不能超过 10 个字符")
    private String name;

    /**
     * 跳转路径
     */
    @NotBlank(message = "路由跳转路径不能为空")
    @Size(max = 100, message = "跳转路径不能超过 100 个字符")
    private String path;

    /**
     * 菜单图标或 Emoji（选填）
     */
    @Size(max = 50, message = "图标标识不能超过 50 个字符")
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
