package org.example.backend.controller.admin.system;

import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.common.BizException;
import org.example.backend.common.UserContext;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vo.MenuTreeVo;
import org.example.backend.entity.SysMenu;
import org.example.backend.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 系统菜单与权限控制器
 */
@RestController
@RequestMapping({"/api/admin/system/menu", "/api/system/menu"})
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    /**
     * 获取当前登录用户可访问的动态路由菜单树（供左侧侧边栏渲染）
     */
    @GetMapping("/routes")
    public BasicResponse<List<MenuTreeVo>> getUserRoutes() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return BasicResponse.error(401, "请先登录");
        }
        List<MenuTreeVo> routes = sysMenuService.getRoutersByUserId(userId);
        return BasicResponse.success(routes);
    }

    /**
     * 获取全量权限树（供角色分配权限弹窗使用，包含 M目录、C菜单、F按钮）
     */
    @GetMapping("/tree")
    public BasicResponse<List<MenuTreeVo>> getAllMenuTree() {
        List<MenuTreeVo> tree = sysMenuService.getAllMenuTree();
        return BasicResponse.success(tree);
    }

    /**
     * 获取菜单管理界面的表格树（包含所有停用/启用的层级）
     */
    @GetMapping("/table")
    @RequiresPermissions("sys:menu:list")
    public BasicResponse<List<MenuTreeVo>> getMenuTableTree() {
        List<MenuTreeVo> tableTree = sysMenuService.getMenuTableTree();
        return BasicResponse.success(tableTree);
    }

    /**
     * 新增菜单/按钮
     */
    @PostMapping("/add")
    @RequiresPermissions("sys:menu:add")
    public BasicResponse<Long> addMenu(@RequestBody SysMenu menu) {
        if (menu == null) {
            throw new BizException("参数不能为空");
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        sysMenuService.save(menu);
        return BasicResponse.success("菜单创建成功", menu.getId());
    }

    /**
     * 修改菜单/按钮
     */
    @PutMapping("/edit")
    @RequiresPermissions("sys:menu:edit")
    public BasicResponse<Void> editMenu(@RequestBody SysMenu menu) {
        if (menu == null || menu.getId() == null) {
            throw new BizException("菜单ID不能为空");
        }
        if (menu.getId().equals(menu.getParentId())) {
            throw new BizException("上级菜单不能选择自身");
        }
        sysMenuService.updateById(menu);
        return BasicResponse.success("菜单修改成功", null);
    }

    /**
     * 删除菜单/按钮
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:menu:delete")
    public BasicResponse<Void> deleteMenu(@PathVariable Long id) {
        if (id == null) {
            throw new BizException("菜单ID不能为空");
        }
        if (sysMenuService.hasChildByMenuId(id)) {
            throw new BizException("存在子菜单或按钮，不允许直接删除！请先删除子项。");
        }
        sysMenuService.removeById(id);
        return BasicResponse.success("菜单删除成功", null);
    }
}
