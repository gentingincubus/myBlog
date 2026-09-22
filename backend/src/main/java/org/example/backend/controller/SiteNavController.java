package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.SiteNavReqDto;
import org.example.backend.entity.SiteNav;
import org.example.backend.service.ISiteNavService;
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
 * 站点导航菜单控制器（受 JWT 拦截器保护）
 */
@RestController
@RequestMapping("/api/nav")
public class SiteNavController {

    @Autowired
    private ISiteNavService siteNavService;

    /**
     * 新增导航菜单
     * @param dto 入参，使用 @Valid 触发 Spring Validation 注解校验
     */
    @PostMapping
    public BasicResponse<Long> addNav(@RequestBody @Valid SiteNavReqDto dto) {
        Long id = siteNavService.addNav(dto);
        return BasicResponse.success(id);
    }

    /**
     * 修改导航菜单
     */
    @PutMapping("/{id}")
    public BasicResponse<String> updateNav(@PathVariable Long id, @RequestBody @Valid SiteNavReqDto dto) {
        siteNavService.updateNav(id, dto);
        return BasicResponse.success("导航菜单修改成功");
    }

    /**
     * 逻辑删除导航菜单
     */
    @DeleteMapping("/{id}")
    public BasicResponse<String> deleteNav(@PathVariable Long id) {
        siteNavService.deleteNav(id);
        return BasicResponse.success("导航菜单删除成功");
    }

    /**
     * 获取未删除的导航菜单列表（带跨表创建人与派生计算排序）
     */
    @GetMapping("/list")
    public BasicResponse<List<org.example.backend.dto.vo.SiteNavVo>> listNavs(org.example.backend.dto.SiteNavQueryDto queryDto) {
        List<org.example.backend.dto.vo.SiteNavVo> list = siteNavService.listNavs(queryDto);
        return BasicResponse.success(list);
    }
}
