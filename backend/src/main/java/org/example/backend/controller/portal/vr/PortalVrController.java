package org.example.backend.controller.portal.vr;

import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vr.VrCategoryQueryDto;
import org.example.backend.dto.vr.VrSceneQueryDto;
import org.example.backend.dto.vr.vo.VrCategoryVo;
import org.example.backend.dto.vr.vo.VrSceneVo;
import org.example.backend.service.vr.IVrCategoryService;
import org.example.backend.service.vr.IVrSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台门户 - VR 全景漫游访客端控制器（公开开放，免登录鉴权）
 */
@RestController
@RequestMapping({"/api/portal/vr", "/api/vr/open"})
public class PortalVrController {

    @Autowired
    private IVrCategoryService vrCategoryService;

    @Autowired
    private IVrSceneService vrSceneService;

    /**
     * 前台查询可用 VR 分类列表
     */
    @GetMapping("/category/list")
    public BasicResponse<List<VrCategoryVo>> listCategories(VrCategoryQueryDto queryDto) {
        // 前台仅展示启用状态的分类
        if (queryDto == null) {
            queryDto = new VrCategoryQueryDto();
        }
        queryDto.setStatus(1);
        List<VrCategoryVo> list = vrCategoryService.listCategories(queryDto);
        return BasicResponse.success(list);
    }

    /**
     * 前台查询单个 VR 分类详情
     */
    @GetMapping("/category/detail/{id}")
    public BasicResponse<VrCategoryVo> getCategoryDetail(@PathVariable Long id) {
        VrCategoryVo vo = vrCategoryService.getCategoryById(id);
        return BasicResponse.success(vo);
    }

    /**
     * 前台根据分类查询 VR 全景点位场景列表
     */
    @GetMapping("/scene/list")
    public BasicResponse<List<VrSceneVo>> listScenes(VrSceneQueryDto queryDto) {
        if (queryDto == null) {
            queryDto = new VrSceneQueryDto();
        }
        queryDto.setStatus(1);
        List<VrSceneVo> list = vrSceneService.listScenes(queryDto);
        return BasicResponse.success(list);
    }

    /**
     * 前台查询单个全景点位详情（全景大图、打点数据、视角配置）
     */
    @GetMapping("/scene/detail/{id}")
    public BasicResponse<VrSceneVo> getSceneDetail(@PathVariable Long id) {
        VrSceneVo vo = vrSceneService.getSceneById(id);
        return BasicResponse.success(vo);
    }
}
