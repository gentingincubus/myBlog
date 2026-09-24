package org.example.backend.controller.vr;

import jakarta.validation.Valid;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vr.VrSceneQueryDto;
import org.example.backend.dto.vr.VrSceneReqDto;
import org.example.backend.dto.vr.vo.VrSceneVo;
import org.example.backend.service.vr.IVrSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * VR 全景场景点位控制器
 */
@RestController
@RequestMapping("/api/vr/scene")
public class VrSceneController {

    @Autowired
    private IVrSceneService vrSceneService;

    /**
     * 新增 VR 场景（需登录）
     */
    @PostMapping
    public BasicResponse<Long> addScene(@RequestBody @Valid VrSceneReqDto dto) {
        Long id = vrSceneService.addScene(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改 VR 场景（需登录）
     */
    @PutMapping("/{id}")
    public BasicResponse<String> updateScene(@PathVariable Long id, @RequestBody @Valid VrSceneReqDto dto) {
        vrSceneService.updateScene(id, dto);
        return BasicResponse.success("VR 场景更新成功");
    }

    /**
     * 逻辑删除 VR 场景（需登录）
     */
    @DeleteMapping("/{id}")
    public BasicResponse<String> deleteScene(@PathVariable Long id) {
        vrSceneService.deleteScene(id);
        return BasicResponse.success("VR 场景删除成功");
    }

    /**
     * 查询 VR 场景列表（公开开放，支持前端及管理端按 categoryId 查阅）
     */
    @GetMapping("/list")
    public BasicResponse<List<VrSceneVo>> listScenes(VrSceneQueryDto queryDto) {
        List<VrSceneVo> list = vrSceneService.listScenes(queryDto);
        return BasicResponse.success(list);
    }

    /**
     * 查询单个 VR 场景详情（公开开放）
     */
    @GetMapping("/detail/{id}")
    public BasicResponse<VrSceneVo> getSceneDetail(@PathVariable Long id) {
        VrSceneVo vo = vrSceneService.getSceneById(id);
        return BasicResponse.success(vo);
    }
}

