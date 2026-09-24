package org.example.backend.service.vr;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.vr.VrCategoryQueryDto;
import org.example.backend.dto.vr.VrCategoryReqDto;
import org.example.backend.dto.vr.vo.VrCategoryVo;
import org.example.backend.entity.vr.VrCategory;

import java.util.List;

/**
 * VR 全景分类业务服务接口
 */
public interface IVrCategoryService extends IService<VrCategory> {

    /**
     * 新增 VR 分类
     * @param dto 分类信息入参
     * @return 生成的雪花 ID
     */
    Long addCategory(VrCategoryReqDto dto);

    /**
     * 修改 VR 分类
     * @param id 分类 ID
     * @param dto 修改参数
     */
    void updateCategory(Long id, VrCategoryReqDto dto);

    /**
     * 逻辑删除 VR 分类（若包含子场景则拦截拒绝）
     * @param id 分类 ID
     */
    void deleteCategory(Long id);

    /**
     * 查询 VR 分类列表（支持按名称、编码、状态过滤，附带场景数量统计）
     * @param queryDto 查询参数
     * @return 分类 VO 列表
     */
    List<VrCategoryVo> listCategories(VrCategoryQueryDto queryDto);

    /**
     * 根据 ID 获取分类详情
     * @param id 分类 ID
     * @return 分类 VO
     */
    VrCategoryVo getCategoryById(Long id);
}

