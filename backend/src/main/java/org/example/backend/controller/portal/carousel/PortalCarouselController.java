package org.example.backend.controller.portal.carousel;

import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.carousel.SiteCarouselVo;
import org.example.backend.service.ISiteCarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台门户 - 首页 3D 毛玻璃轮播图公开查询控制器（公开免登）
 */
@RestController
@RequestMapping({"/api/portal/carousel", "/api/carousel/portal"})
public class PortalCarouselController {

    @Autowired
    private ISiteCarouselService siteCarouselService;

    /**
     * 前台获取已启用的轮播图列表
     */
    @GetMapping("/list")
    public BasicResponse<List<SiteCarouselVo>> listCarousels() {
        List<SiteCarouselVo> list = siteCarouselService.listPortalCarousels();
        return BasicResponse.success(list);
    }
}
