package org.example.backend.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户视图对象（VO / View Object）
 * 专供前端页面展示使用：只包含安全公开的字段，从物理上彻底杜绝密码泄露
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo implements Serializable {

    /** 用户主键ID */
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像链接 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 状态：1正常，0禁用 */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime createTime;
}
