package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SysRole;

/**
 * 角色数据访问接口
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}

