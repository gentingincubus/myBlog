package org.example.backend.controller.admin.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vo.UserVo;
import org.example.backend.entity.SysRole;
import org.example.backend.service.ISysRoleService;
import org.example.backend.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台 - 系统用户管理控制器
 */
@RestController
@RequestMapping({"/api/admin/system/user", "/api/system/user"})
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysRoleService sysRoleService;

    /**
     * 分页查询系统用户列表
     */
    @GetMapping("/page")
    @RequiresPermissions("sys:user:list")
    public BasicResponse<IPage<UserVo>> getUserPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        IPage<UserVo> page = sysUserService.getUserPage(pageNum, pageSize, username, status);
        return BasicResponse.success(page);
    }

    /**
     * 获取指定用户已绑定的角色ID列表
     */
    @GetMapping("/{userId}/roles")
    @RequiresPermissions("sys:user:role")
    public BasicResponse<List<Long>> getUserRoles(@PathVariable Long userId) {
        List<SysRole> roles = sysRoleService.getRolesByUserId(userId);
        List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());
        return BasicResponse.success(roleIds);
    }

    /**
     * 为指定用户分配角色
     */
    @PostMapping("/{userId}/roles")
    @RequiresPermissions("sys:user:role")
    public BasicResponse<String> assignUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> req) {
        List<Long> roleIds = req.get("roleIds");
        sysRoleService.assignUserRoles(userId, roleIds);
        return BasicResponse.success("角色分配成功");
    }

    /**
     * 启用/停用系统用户
     */
    @PutMapping("/{userId}/status")
    @RequiresPermissions("sys:user:status")
    public BasicResponse<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        sysUserService.updateUserStatus(userId, status);
        return BasicResponse.success("用户状态更新成功");
    }

    /**
     * 管理员重置指定用户密码
     */
    @PostMapping("/{userId}/resetPwd")
    @RequiresPermissions("sys:user:resetPwd")
    public BasicResponse<String> resetUserPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> req) {
        String newPassword = req.get("newPassword");
        sysUserService.resetPassword(userId, newPassword);
        return BasicResponse.success("密码重置成功");
    }
}
