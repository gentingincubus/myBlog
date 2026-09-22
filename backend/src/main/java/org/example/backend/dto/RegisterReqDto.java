package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户注册请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterReqDto implements Serializable {

    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;

    /** 用户昵称（选填，默认同用户名） */
    private String nickname;
}
