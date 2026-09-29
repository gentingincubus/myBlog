package org.example.backend.controller.common;

import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.LoginReqDto;
import org.example.backend.dto.LoginRespDto;
import org.example.backend.dto.RegisterReqDto;
import org.example.backend.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通用能力 - 认证与注册控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public BasicResponse<LoginRespDto> login(@RequestBody LoginReqDto loginReqDto) {
        LoginRespDto loginResp = sysUserService.login(loginReqDto);
        return BasicResponse.success(loginResp);
    }

    /**
     * 用户注册接口（注册成功后默认绑定 common 普通用户角色）
     */
    @PostMapping("/register")
    public BasicResponse<String> register(@RequestBody RegisterReqDto registerReqDto) {
        sysUserService.register(registerReqDto);
        return BasicResponse.success("注册成功");
    }
}
