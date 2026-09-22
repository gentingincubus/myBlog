package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录请求入参 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginReqDto implements Serializable {

    /** 登录用户名 */
    private String username;

    /** 登录密码 */
    private String password;
}
