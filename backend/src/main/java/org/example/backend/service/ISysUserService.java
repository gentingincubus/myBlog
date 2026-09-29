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

    /**
     * 修改密码（无需验证旧密码，双重确认新密码）
     *
     * @param reqDto 修改密码入参
     */
    void changePassword(org.example.backend.dto.ChangePasswordReqDto reqDto);

    /**
     * 获取当前登录用户的综合资料（包含脱敏信息、角色列表、权限字符集合）
     */
    org.example.backend.dto.UserInfoRespDto getCurrentUserInfo();

    /**
     * 分页查询系统用户列表
     */
    com.baomidou.mybatisplus.core.metadata.IPage<org.example.backend.dto.vo.UserVo> getUserPage(int pageNum, int pageSize, String username, Integer status);

    /**
     * 修改用户账号启用/禁用状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 管理员重置用户密码
     */
    void resetPassword(Long userId, String newPassword);
}
