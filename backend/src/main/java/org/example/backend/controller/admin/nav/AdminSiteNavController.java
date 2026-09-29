package org.example.backend.controller.admin.nav;

import jakarta.validation.Valid;
import org.example.backend.annotation.RequiresPermissions;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.SiteNavQueryDto;
import org.example.backend.dto.SiteNavReqDto;
import org.example.backend.dto.vo.SiteNavVo;
import org.example.backend.service.ISiteNavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 站点导航管理控制器
 */
@RestController
@RequestMapping({"/api/admin/nav", "/api/nav"})
public class AdminSiteNavController {

    @Autowired
    private ISiteNavService siteNavService;

    /**
     * 新增导航菜单
     */
    @PostMapping
    @RequiresPermissions("site:nav:add")
    public BasicResponse<Long> addNav(@RequestBody @Valid SiteNavReqDto dto) {
        Long id = siteNavService.addNav(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改导航菜单
     */
    @PutMapping("/{id}")
    @RequiresPermissions("site:nav:edit")
    public BasicResponse<String> updateNav(@PathVariable Long id, @RequestBody @Valid SiteNavReqDto dto) {
        siteNavService.updateNav(id, dto);
        return BasicResponse.success("导航菜单修改成功");
    }

    /**
     * 逻辑删除导航菜单
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("site:nav:delete")
    public BasicResponse<String> deleteNav(@PathVariable Long id) {
        siteNavService.deleteNav(id);
        return BasicResponse.success("导航菜单删除成功");
    }

    /**
     * 后台获取导航菜单列表
     */
    @GetMapping("/list")
    public BasicResponse<List<SiteNavVo>> listNavs(SiteNavQueryDto queryDto) {
        List<SiteNavVo> list = siteNavService.listNavs(queryDto);
        return BasicResponse.success(list);
    }
}
