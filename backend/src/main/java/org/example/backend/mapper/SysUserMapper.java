package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SysUser;

/**
 * 用户数据访问接口（Mapper）
 * 继承 BaseMapper 后，立即拥有增删改查（insert, deleteById, updateById, selectById, selectList 等）所有内置能力！
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 无需手动写任何基础 CRUD 的 SQL，MyBatis-Plus 已全部自动实现
}

