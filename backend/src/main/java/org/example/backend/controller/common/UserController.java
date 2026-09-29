package org.example.backend.controller.common;

import org.example.backend.dto.BasicResponse;
import org.example.backend.dto.ChangePasswordReqDto;
import org.example.backend.dto.UserInfoRespDto;
import org.example.backend.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通用能力 - 个人中心控制器（当前登录用户操作，受 JWT 保护）
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 获取当前登录用户的综合信息（包括脱敏资料、角色列表、按钮权限标识列表）
     */
    @GetMapping("/info")
    public BasicResponse<UserInfoRespDto> getUserInfo() {
        UserInfoRespDto userInfo = sysUserService.getCurrentUserInfo();
        return BasicResponse.success(userInfo);
    }

    /**
     * 修改当前登录用户密码（无需输入旧密码，需确认两次新密码防手误）
     */
    @PostMapping("/password")
    public BasicResponse<String> changePassword(@RequestBody ChangePasswordReqDto reqDto) {
        sysUserService.changePassword(reqDto);
        return BasicResponse.success("密码修改成功");
    }
}
