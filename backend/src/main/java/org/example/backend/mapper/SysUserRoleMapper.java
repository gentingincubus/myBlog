package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SysUserRole;

/**
 * 用户角色关联数据访问接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}

