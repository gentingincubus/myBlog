package org.example.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.dto.vo.MenuTreeVo;
import org.example.backend.entity.SysMenu;
import org.example.backend.entity.SysRoleMenu;
import org.example.backend.entity.SysUserRole;
import org.example.backend.mapper.SysMenuMapper;
import org.example.backend.mapper.SysRoleMenuMapper;
import org.example.backend.mapper.SysUserRoleMapper;
import org.example.backend.service.ISysMenuService;
import org.example.backend.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单与权限业务实现类
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Autowired
    private ISysRoleService sysRoleService;

    @Override
    public Set<String> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        // 1. 超管特权通道：直接持有全站最高特权字符
        Set<String> roleKeys = sysRoleService.getRoleKeysByUserId(userId);
        if (userId == 1L || roleKeys.contains("admin")) {
            return Set.of("*:*:*");
        }

        // 2. 普通用户：获取所有角色关联的权限字符
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (CollUtil.isEmpty(userRoles)) {
            return Collections.emptySet();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
        );
        if (CollUtil.isEmpty(roleMenus)) {
            return Collections.emptySet();
        }

        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
        List<SysMenu> menus = this.list(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getStatus, 1)
        );

        return menus.stream()
                .map(SysMenu::getPerms)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    @Override
    public List<MenuTreeVo> getRoutersByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        Set<String> roleKeys = sysRoleService.getRoleKeysByUserId(userId);
        List<SysMenu> rawMenus;

        if (userId == 1L || roleKeys.contains("admin")) {
            // 超管加载所有已启用的目录和菜单 (过滤 F 按钮)
            rawMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getMenuType, "M", "C")
                    .eq(SysMenu::getStatus, 1)
                    .orderByAsc(SysMenu::getSort));
        } else {
            // 普通用户按其所持角色授权过滤
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
            );
            if (CollUtil.isEmpty(userRoles)) {
                return Collections.emptyList();
            }
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds)
            );
            if (CollUtil.isEmpty(roleMenus)) {
                return Collections.emptyList();
            }
            List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());
            rawMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, menuIds)
                    .in(SysMenu::getMenuType, "M", "C")
                    .eq(SysMenu::getStatus, 1)
                    .orderByAsc(SysMenu::getSort));
        }

        return buildTree(rawMenus, 0L);
    }

    @Override
    public List<MenuTreeVo> getAllMenuTree() {
        // 全量树（供分配角色权限使用，包含 M、C、F）
        List<SysMenu> list = this.list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));
        return buildTree(list, 0L);
    }

    @Override
    public List<MenuTreeVo> getMenuTableTree() {
        // 菜单管理表格展示树（包含停用的）
        List<SysMenu> list = this.list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return buildTree(list, 0L);
    }

    @Override
    public boolean hasChildByMenuId(Long menuId) {
        if (menuId == null) {
            return false;
        }
        long count = this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId));
        return count > 0;
    }

    /**
     * 通用递归构建树形结构算法
     */
    private List<MenuTreeVo> buildTree(List<SysMenu> menuList, Long rootParentId) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }

        Map<Long, List<MenuTreeVo>> parentMap = new HashMap<>();
        for (SysMenu m : menuList) {
            MenuTreeVo vo = MenuTreeVo.builder()
                    .id(m.getId())
                    .parentId(m.getParentId())
                    .menuName(m.getMenuName())
                    .menuType(m.getMenuType())
                    .path(m.getPath())
                    .component(m.getComponent())
                    .perms(m.getPerms())
                    .icon(m.getIcon())
                    .sort(m.getSort())
                    .visible(m.getVisible())
                    .status(m.getStatus())
                    .createTime(m.getCreateTime())
                    .children(new ArrayList<>())
                    .build();

            Long pid = m.getParentId() != null ? m.getParentId() : 0L;
            parentMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(vo);
        }

        List<MenuTreeVo> roots = parentMap.getOrDefault(rootParentId, new ArrayList<>());
        for (MenuTreeVo root : roots) {
            fillChildren(root, parentMap);
        }
        return roots;
    }

    private void fillChildren(MenuTreeVo parent, Map<Long, List<MenuTreeVo>> parentMap) {
        List<MenuTreeVo> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (MenuTreeVo child : children) {
                fillChildren(child, parentMap);
            }
        }
    }
}

