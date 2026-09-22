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
import org.springframework.stereotype.Service;

/**
 * 用户业务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

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
    }
}
