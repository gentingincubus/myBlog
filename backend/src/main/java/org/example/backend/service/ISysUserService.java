package org.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.backend.dto.LoginReqDto;
import org.example.backend.dto.LoginRespDto;
import org.example.backend.dto.RegisterReqDto;
import org.example.backend.entity.SysUser;

/**
 * 用户业务接口
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户账号密码登录
     *
     * @param loginReqDto 登录入参（用户名和密码）
     * @return 登录结果（Token 与 用户信息）
     */
    LoginRespDto login(LoginReqDto loginReqDto);

    /**
     * 新用户注册
     *
     * @param registerReqDto 注册入参（用户名、密码、昵称）
     */
    void register(RegisterReqDto registerReqDto);
}
