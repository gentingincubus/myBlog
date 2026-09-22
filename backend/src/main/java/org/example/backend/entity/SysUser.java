package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类（对应数据库 sys_user 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user") // 告诉 MyBatis-Plus 对应哪张表
public class SysUser implements Serializable {

    /** 主键ID，设置自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像链接 */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 状态：1正常，0禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

