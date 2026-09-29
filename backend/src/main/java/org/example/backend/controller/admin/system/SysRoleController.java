package org.example.backend.controller.admin.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.common.BizException;
import org.example.backend.dto.BasicResponse;
import org.example.backend.entity.SysRole;
import org.example.backend.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 系统角色控制器
 */
@RestController
@RequestMapping({"/api/admin/system/role", "/api/system/role"})
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    @RequiresPermissions("sys:role:list")
    public BasicResponse<List<SysRole>> getRoleList() {
        List<SysRole> list = sysRoleService.list(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort)
        );
        return BasicResponse.success(list);
    }

    /**
     * 获取指定角色已绑定的菜单与按钮ID列表
     */
    @GetMapping("/{roleId}/menus")
    @RequiresPermissions("sys:role:perm")
    public BasicResponse<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        List<Long> menuIds = sysRoleService.getRoleMenuIds(roleId);
        return BasicResponse.success(menuIds);
    }

    /**
     * 为角色分配菜单与按钮权限
     */
    @PostMapping("/{roleId}/menus")
    @RequiresPermissions("sys:role:perm")
    public BasicResponse<Void> assignRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        if (roleId == 1L) {
            throw new BizException("超级管理员拥有全量特权，无需单独配置权限");
        }
        sysRoleService.assignRoleMenus(roleId, menuIds);
        return BasicResponse.success("权限分配成功", null);
    }

    /**
     * 新增角色
     */
    @PostMapping("/add")
    @RequiresPermissions("sys:role:add")
    public BasicResponse<Long> addRole(@RequestBody SysRole role) {
        if (role == null) {
            throw new BizException("参数不能为空");
        }
        long count = sysRoleService.count(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, role.getRoleKey())
        );
        if (count > 0) {
            throw new BizException("角色权限字符已存在，请更换！");
        }
        sysRoleService.save(role);
        return BasicResponse.success("角色创建成功", role.getId());
    }

    /**
     * 修改角色
     */
    @PutMapping("/edit")
    @RequiresPermissions("sys:role:edit")
    public BasicResponse<Void> editRole(@RequestBody SysRole role) {
        if (role == null || role.getId() == null) {
            throw new BizException("角色ID不能为空");
        }
        if (role.getId() == 1L && !"admin".equals(role.getRoleKey())) {
            throw new BizException("超级管理员角色标识不可修改");
        }
        sysRoleService.updateById(role);
        return BasicResponse.success("角色修改成功", null);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:role:delete")
    public BasicResponse<Void> deleteRole(@PathVariable Long id) {
        if (id == null) {
            throw new BizException("角色ID不能为空");
        }
        if (id == 1L || id == 2L) {
            throw new BizException("系统内置角色（超级管理员、普通用户）禁止删除！");
        }
        sysRoleService.removeById(id);
        return BasicResponse.success("角色删除成功", null);
    }
}
