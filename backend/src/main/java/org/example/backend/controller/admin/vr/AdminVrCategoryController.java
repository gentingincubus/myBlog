package org.example.backend.controller.admin.vr;

import jakarta.validation.Valid;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vr.VrCategoryQueryDto;
import org.example.backend.dto.vr.VrCategoryReqDto;
import org.example.backend.dto.vr.vo.VrCategoryVo;
import org.example.backend.service.vr.IVrCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - VR 园区/分类管理控制器
 */
@RestController
@RequestMapping({"/api/admin/vr/category", "/api/vr/category"})
public class AdminVrCategoryController {

    @Autowired
    private IVrCategoryService vrCategoryService;

    /**
     * 新增 VR 分类（需具备新增权限）
     */
    @PostMapping
    @RequiresPermissions("vr:category:add")
    public BasicResponse<Long> addCategory(@RequestBody @Valid VrCategoryReqDto dto) {
        Long id = vrCategoryService.addCategory(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改 VR 分类（需具备修改权限）
     */
    @PutMapping("/{id}")
    @RequiresPermissions("vr:category:edit")
    public BasicResponse<String> updateCategory(@PathVariable Long id, @RequestBody @Valid VrCategoryReqDto dto) {
        vrCategoryService.updateCategory(id, dto);
        return BasicResponse.success("VR 分类更新成功");
    }

    /**
     * 逻辑删除 VR 分类（需具备删除权限）
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("vr:category:delete")
    public BasicResponse<String> deleteCategory(@PathVariable Long id) {
        vrCategoryService.deleteCategory(id);
        return BasicResponse.success("VR 分类删除成功");
    }

    /**
     * 后台查询 VR 分类列表
     */
    @GetMapping("/list")
    @RequiresPermissions("vr:category:list")
    public BasicResponse<List<VrCategoryVo>> listCategories(VrCategoryQueryDto queryDto) {
        List<VrCategoryVo> list = vrCategoryService.listCategories(queryDto);
        return BasicResponse.success(list);
    }

    /**
     * 查询单个 VR 分类详情
     */
    @GetMapping("/detail/{id}")
    public BasicResponse<VrCategoryVo> getCategoryDetail(@PathVariable Long id) {
        VrCategoryVo vo = vrCategoryService.getCategoryById(id);
        return BasicResponse.success(vo);
    }
}
