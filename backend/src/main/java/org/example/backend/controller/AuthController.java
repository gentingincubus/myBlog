package org.example.backend.controller;

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
 * 认证与授权控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 用户登录接口
     *
     * @param loginReqDto 登录入参（JSON 格式传输）
     * @return 登录结果及 Token
     */
    @PostMapping("/login")
    public BasicResponse<LoginRespDto> login(@RequestBody LoginReqDto loginReqDto) {
        LoginRespDto loginResp = sysUserService.login(loginReqDto);
        return BasicResponse.success(loginResp);
    }

    /**
     * 用户注册接口
     *
     * @param registerReqDto 注册入参（JSON 格式传输）
     * @return 统一响应
     */
    @PostMapping("/register")
    public BasicResponse<String> register(@RequestBody RegisterReqDto registerReqDto) {
        sysUserService.register(registerReqDto);
        return BasicResponse.success("注册成功");
    }
}
