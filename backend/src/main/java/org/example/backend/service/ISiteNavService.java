package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.SiteNavReqDto;
import org.example.backend.dto.vo.SiteNavVo;
import org.example.backend.entity.SiteNav;

import java.util.List;

/**
 * 站点导航服务接口
 */
public interface ISiteNavService extends IService<SiteNav> {

    /**
     * 新增导航菜单
     * @param dto 入参
     * @return 新生成的雪花算法 ID
     */
    Long addNav(SiteNavReqDto dto);

    /**
     * 修改导航菜单
     * @param id 菜单ID
     * @param dto 入参
     */
    void updateNav(Long id, SiteNavReqDto dto);

    /**
     * 逻辑删除导航菜单
     * @param id 菜单ID
     */
    void deleteNav(Long id);

    /**
     * 根据筛选条件获取未删除的导航菜单列表（内存组装返回 VO）
     * @param queryDto 筛选条件（可为 null）
     */
    List<SiteNavVo> listNavs(org.example.backend.dto.SiteNavQueryDto queryDto);

    /**
     * 获取所有未删除的导航菜单列表（重载）
     */
    default List<SiteNavVo> listNavs() {
        return listNavs(null);
    }

    /**
     * 【对比实验 A】：无事务测试（故意中途抛异常）
     */
    void batchAddWithoutTx(boolean makeError);

    /**
     * 【对比实验 B】：声明式事务测试（@Transactional 自动回滚）
     */
    void batchAddWithTx(boolean makeError);

    /**
     * 清理事务测试残留的测试数据
     */
    void cleanTxTestData();
}
