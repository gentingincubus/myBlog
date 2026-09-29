package org.example.backend.controller.admin.vr;

import jakarta.validation.Valid;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vr.VrSceneQueryDto;
import org.example.backend.dto.vr.VrSceneReqDto;
import org.example.backend.dto.vr.vo.VrSceneVo;
import org.example.backend.service.vr.IVrSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - VR 场景点位管理控制器
 */
@RestController
@RequestMapping({"/api/admin/vr/scene", "/api/vr/scene"})
public class AdminVrSceneController {

    @Autowired
    private IVrSceneService vrSceneService;

    /**
     * 新增 VR 场景（需具备新增权限）
     */
    @PostMapping
    @RequiresPermissions("vr:scene:add")
    public BasicResponse<Long> addScene(@RequestBody @Valid VrSceneReqDto dto) {
        Long id = vrSceneService.addScene(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改 VR 场景（需具备修改权限）
     */
    @PutMapping("/{id}")
    @RequiresPermissions("vr:scene:edit")
    public BasicResponse<String> updateScene(@PathVariable Long id, @RequestBody @Valid VrSceneReqDto dto) {
        vrSceneService.updateScene(id, dto);
        return BasicResponse.success("VR 场景更新成功");
    }

    /**
     * 逻辑删除 VR 场景（需具备删除权限）
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("vr:scene:delete")
    public BasicResponse<String> deleteScene(@PathVariable Long id) {
        vrSceneService.deleteScene(id);
        return BasicResponse.success("VR 场景删除成功");
    }

    /**
     * 后台查询 VR 场景列表
     */
    @GetMapping("/list")
    @RequiresPermissions("vr:scene:list")
    public BasicResponse<List<VrSceneVo>> listScenes(VrSceneQueryDto queryDto) {
        List<VrSceneVo> list = vrSceneService.listScenes(queryDto);
        return BasicResponse.success(list);
    }

    /**
     * 查询单个 VR 场景详情
     */
    @GetMapping("/detail/{id}")
    public BasicResponse<VrSceneVo> getSceneDetail(@PathVariable Long id) {
        VrSceneVo vo = vrSceneService.getSceneById(id);
        return BasicResponse.success(vo);
    }
}
