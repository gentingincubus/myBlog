package org.example.backend.controller.admin.carousel;

import jakarta.validation.Valid;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.carousel.SiteCarouselQueryDto;
import org.example.backend.dto.carousel.SiteCarouselReqDto;
import org.example.backend.dto.carousel.SiteCarouselVo;
import org.example.backend.service.ISiteCarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 首页轮播图管理控制器
 */
@RestController
@RequestMapping({"/api/admin/carousel", "/api/carousel"})
public class AdminCarouselController {

    @Autowired
    private ISiteCarouselService siteCarouselService;

    /**
     * 新增轮播图
     */
    @PostMapping
    @RequiresPermissions("site:carousel:add")
    public BasicResponse<Long> addCarousel(@RequestBody @Valid SiteCarouselReqDto dto) {
        Long id = siteCarouselService.addCarousel(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改轮播图
     */
    @PutMapping("/{id}")
    @RequiresPermissions("site:carousel:edit")
    public BasicResponse<String> updateCarousel(@PathVariable Long id, @RequestBody @Valid SiteCarouselReqDto dto) {
        siteCarouselService.updateCarousel(id, dto);
        return BasicResponse.success("轮播图修改成功");
    }

    /**
     * 逻辑删除轮播图
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("site:carousel:delete")
    public BasicResponse<String> deleteCarousel(@PathVariable Long id) {
        siteCarouselService.deleteCarousel(id);
        return BasicResponse.success("轮播图删除成功");
    }

    /**
     * 更新启停状态
     */
    @PutMapping("/{id}/status")
    @RequiresPermissions("site:carousel:edit")
    public BasicResponse<String> updateStatus(@PathVariable Long id, @RequestParam("status") Integer status) {
        siteCarouselService.updateStatus(id, status);
        return BasicResponse.success("状态更新成功");
    }

    /**
     * 后台获取轮播图列表
     */
    @GetMapping("/list")
    @RequiresPermissions("site:carousel:list")
    public BasicResponse<List<SiteCarouselVo>> listCarousels(SiteCarouselQueryDto queryDto) {
        List<SiteCarouselVo> list = siteCarouselService.listAdminCarousels(queryDto);
        return BasicResponse.success(list);
    }
}
