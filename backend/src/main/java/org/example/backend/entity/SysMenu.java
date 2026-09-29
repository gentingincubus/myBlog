package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 菜单与权限实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父菜单ID (顶级为0) */
    private Long parentId;

    /** 菜单/按钮名称 */
    private String menuName;

    /** 菜单类型 (M: 目录, C: 菜单, F: 按钮操作) */
    private String menuType;

    /** 路由地址 (仅目录/菜单有效) */
    private String path;

    /** 组件路径 (如 admin/vr/VrSceneManageView) */
    private String component;

    /** 权限标识 (如 vr:scene:add, 仅按钮/菜单有效) */
    private String perms;

    /** 菜单图标 (如 Odometer, PictureFilled) */
    private String icon;

    /** 显示顺序 */
    private Integer sort;

    /** 是否可见 (1: 显示, 0: 隐藏) */
    private Integer visible;

    /** 菜单状态 (1: 正常, 0: 停用) */
    private Integer status;
}

