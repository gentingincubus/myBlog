package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.dto.vo.UserVo;

import java.io.Serializable;

/**
 * 登录成功响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespDto implements Serializable {

    /** 数字通行证 Token */
    private String token;

    /** 当前登录的用户信息（安全的 UserVo 对象） */
    private UserVo userInfo;
}
