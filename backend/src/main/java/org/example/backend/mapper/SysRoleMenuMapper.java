package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SysRoleMenu;

/**
 * 角色菜单关联数据访问接口
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}

