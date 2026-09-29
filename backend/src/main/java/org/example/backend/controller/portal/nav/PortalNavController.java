package org.example.backend.controller.portal.nav;

import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.SiteNavQueryDto;
import org.example.backend.dto.vo.SiteNavVo;
import org.example.backend.service.ISiteNavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台门户 - 站点导航公开查询控制器（公开免登）
 */
@RestController
@RequestMapping({"/api/portal/nav", "/api/nav/portal"})
public class PortalNavController {

    @Autowired
    private ISiteNavService siteNavService;

    /**
     * 前台获取站点导航列表
     */
    @GetMapping("/list")
    public BasicResponse<List<SiteNavVo>> listNavs(SiteNavQueryDto queryDto) {
        List<SiteNavVo> list = siteNavService.listNavs(queryDto);
        return BasicResponse.success(list);
    }
}
