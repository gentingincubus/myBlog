package org.example.backend.service.vr;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.vr.VrSceneQueryDto;
import org.example.backend.dto.vr.VrSceneReqDto;
import org.example.backend.dto.vr.vo.VrSceneVo;
import org.example.backend.entity.vr.VrScene;

import java.util.List;

/**
 * VR 全景场景业务服务接口
 */
public interface IVrSceneService extends IService<VrScene> {

    /**
     * 新增 VR 场景点位
     * @param dto 场景参数
     * @return 生成的雪花 ID
     */
    Long addScene(VrSceneReqDto dto);

    /**
     * 修改 VR 场景点位
     * @param id 场景 ID
     * @param dto 修改参数
     */
    void updateScene(Long id, VrSceneReqDto dto);

    /**
     * 逻辑删除 VR 场景点位
     * @param id 场景 ID
     */
    void deleteScene(Long id);

    /**
     * 查询 VR 场景列表（支持按分类ID、场景名称、状态筛选，按 sort 升序排列）
     * @param queryDto 查询参数
     * @return 场景 VO 列表
     */
    List<VrSceneVo> listScenes(VrSceneQueryDto queryDto);

    /**
     * 根据 ID 获取场景详情
     * @param id 场景 ID
     * @return 场景 VO
     */
    VrSceneVo getSceneById(Long id);
}

