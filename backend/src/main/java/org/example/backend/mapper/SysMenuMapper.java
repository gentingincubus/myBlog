package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SysMenu;

/**
 * 菜单与权限数据访问接口
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}

