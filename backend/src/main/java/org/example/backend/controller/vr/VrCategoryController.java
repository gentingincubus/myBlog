package org.example.backend.controller.vr;

import jakarta.validation.Valid;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.vr.VrCategoryQueryDto;
import org.example.backend.dto.vr.VrCategoryReqDto;
import org.example.backend.dto.vr.vo.VrCategoryVo;
import org.example.backend.service.vr.IVrCategoryService;
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
 * VR 全景分类/园区控制器
 */
@RestController
@RequestMapping("/api/vr/category")
public class VrCategoryController {

    @Autowired
    private IVrCategoryService vrCategoryService;

    /**
     * 新增 VR 分类（需登录）
     */
    @PostMapping
    public BasicResponse<Long> addCategory(@RequestBody @Valid VrCategoryReqDto dto) {
        Long id = vrCategoryService.addCategory(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改 VR 分类（需登录）
     */
    @PutMapping("/{id}")
    public BasicResponse<String> updateCategory(@PathVariable Long id, @RequestBody @Valid VrCategoryReqDto dto) {
        vrCategoryService.updateCategory(id, dto);
        return BasicResponse.success("VR 分类更新成功");
    }

    /**
     * 逻辑删除 VR 分类（需登录）
     */
    @DeleteMapping("/{id}")
    public BasicResponse<String> deleteCategory(@PathVariable Long id) {
        vrCategoryService.deleteCategory(id);
        return BasicResponse.success("VR 分类删除成功");
    }

    /**
     * 查询 VR 分类列表（公开开放，支持前端及管理端查看）
     */
    @GetMapping("/list")
    public BasicResponse<List<VrCategoryVo>> listCategories(VrCategoryQueryDto queryDto) {
        List<VrCategoryVo> list = vrCategoryService.listCategories(queryDto);
        return BasicResponse.success(list);
    }

    /**
     * 查询单个 VR 分类详情（公开开放）
     */
    @GetMapping("/detail/{id}")
    public BasicResponse<VrCategoryVo> getCategoryDetail(@PathVariable Long id) {
        VrCategoryVo vo = vrCategoryService.getCategoryById(id);
        return BasicResponse.success(vo);
    }
}

