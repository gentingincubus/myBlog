package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.backend.entity.SiteNav;

/**
 * 站点导航 Mapper 接口
 */
@Mapper
public interface SiteNavMapper extends BaseMapper<SiteNav> {

    /**
     * 专属物理真删除：专门用于彻底清除事务演练产生的测试数据
     * 绕过 @TableLogic，直接执行原生 DELETE FROM，磁盘完全抹除！
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM site_nav WHERE name LIKE '%测试%'")
    int physicalDeleteTestData();
}
