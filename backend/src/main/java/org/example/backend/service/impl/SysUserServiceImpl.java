package org.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.backend.common.BizException;
import org.example.backend.dto.LoginReqDto;
import org.example.backend.dto.LoginRespDto;
import org.example.backend.dto.RegisterReqDto;
import org.example.backend.dto.vo.UserVo;
import org.example.backend.entity.SysUser;
import org.example.backend.mapper.SysUserMapper;
import org.example.backend.service.ISysUserService;
import org.example.backend.utils.JwtUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.backend.dto.UserInfoRespDto;
import org.example.backend.entity.SysUserRole;
import org.example.backend.mapper.SysUserRoleMapper;
import org.example.backend.service.ISysMenuService;
import org.example.backend.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private ISysRoleService sysRoleService;

    @Autowired
    private ISysMenuService sysMenuService;

    @Override
    public LoginRespDto login(LoginReqDto loginReqDto) {
        // 1. 校验必填入参
        if (loginReqDto == null || StrUtil.isBlank(loginReqDto.getUsername()) || StrUtil.isBlank(loginReqDto.getPassword())) {
            throw new BizException("用户名和密码不能为空");
        }

        String username = loginReqDto.getUsername().trim();
        String password = loginReqDto.getPassword().trim();

        // 2. 根据用户名查询数据库 Entity
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));

        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 3. 校验密码：使用 BCrypt 动态加盐哈希比对
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BizException("密码错误，请重新输入");
        }

        // 4. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("该账号已被禁用，请联系管理员");
        }

        // 5. 签发 JWT 数字通行证 Token
        String token = JwtUtils.createToken(user.getId(), user.getUsername());

        // 6. 实体属性转换（Entity -> VO）
        UserVo userVo = BeanUtil.copyProperties(user, UserVo.class);

        // 7. 组装返回安全的 DTO
        return LoginRespDto.builder()
                .token(token)
                .userInfo(userVo)
                .build();
    }

    @Override
    public void register(RegisterReqDto registerReqDto) {
        // 1. 校验必填入参
        if (registerReqDto == null || StrUtil.isBlank(registerReqDto.getUsername()) || StrUtil.isBlank(registerReqDto.getPassword())) {
            throw new BizException("用户名和密码不能为空");
        }

        String username = registerReqDto.getUsername().trim();
        String password = registerReqDto.getPassword().trim();

        if (username.length() < 3 || username.length() > 20) {
            throw new BizException("用户名长度必须在 3 ~ 20 位之间");
        }
        if (password.length() < 6 || password.length() > 30) {
            throw new BizException("密码长度必须在 6 ~ 30 位之间");
        }

        // 2. 查重：检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (count > 0) {
            throw new BizException("该用户名已被注册，请更换其它用户名");
        }

        // 3. 密码加盐哈希加密（绝不存明文）
        String hashedPassword = BCrypt.hashpw(password);

        // 4. 构造新用户实体并存入 MySQL 数据库
        String nickname = StrUtil.isNotBlank(registerReqDto.getNickname()) ? registerReqDto.getNickname().trim() : username;
        SysUser newUser = SysUser.builder()
                .username(username)
                .password(hashedPassword)
                .nickname(nickname)
                .status(1) // 1 表示正常状态
                .build();

        this.save(newUser);

        // 5. 自动分配默认角色：普通用户 (common, id = 2)
        sysUserRoleMapper.insert(SysUserRole.builder()
                .userId(newUser.getId())
                .roleId(2L)
                .build());
    }

    @Override
    public UserInfoRespDto getCurrentUserInfo() {
        Long userId = org.example.backend.common.UserContext.getUserId();
        if (userId == null) {
            throw new BizException(401, "请先登录");
        }
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        UserVo userVo = BeanUtil.copyProperties(user, UserVo.class);
        Set<String> roles = sysRoleService.getRoleKeysByUserId(userId);
        Set<String> permissions = sysMenuService.getPermissionsByUserId(userId);

        return UserInfoRespDto.builder()
                .user(userVo)
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<UserVo> getUserPage(int pageNum, int pageSize, String username, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>()
                .like(StrUtil.isNotBlank(username), SysUser::getUsername, username)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = this.page(page, queryWrapper);
        Page<UserVo> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVo> voList = userPage.getRecords().stream()
                .map(u -> BeanUtil.copyProperties(u, UserVo.class))
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            throw new BizException("参数不能为空");
        }
        if (userId == 1L && status == 0) {
            throw new BizException("超级管理员账号不可禁用");
        }
        SysUser user = SysUser.builder().id(userId).status(status).build();
        this.updateById(user);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        if (userId == null || StrUtil.isBlank(newPassword)) {
            throw new BizException("用户ID与新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 30) {
            throw new BizException("新密码长度必须在 6 ~ 30 位之间");
        }
        String hashedPassword = BCrypt.hashpw(newPassword.trim());
        SysUser user = SysUser.builder().id(userId).password(hashedPassword).build();
        this.updateById(user);
    }

    @Override
    public void changePassword(org.example.backend.dto.ChangePasswordReqDto reqDto) {
        if (reqDto == null || StrUtil.isBlank(reqDto.getNewPassword()) || StrUtil.isBlank(reqDto.getConfirmPassword())) {
            throw new BizException("新密码与确认密码均不能为空");
        }

        String newPassword = reqDto.getNewPassword().trim();
        String confirmPassword = reqDto.getConfirmPassword().trim();

        if (newPassword.length() < 6 || newPassword.length() > 30) {
            throw new BizException("密码长度必须在 6 ~ 30 位之间");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BizException("两次输入的新密码不一致，请仔细核对");
        }

        Long userId = org.example.backend.common.UserContext.getUserId();
        if (userId == null) {
            throw new BizException(401, "登录已失效，请重新登录后再试");
        }

        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BizException("当前用户不存在");
        }

        // 密码加盐哈希加密更新
        String hashedPassword = BCrypt.hashpw(newPassword);
        SysUser updateUser = SysUser.builder()
                .id(userId)
                .password(hashedPassword)
                .build();

        this.updateById(updateUser);
    }
}
