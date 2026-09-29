package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.dto.vo.UserVo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 当前登录用户信息响应 DTO（包含基础资料、角色编码列表、权限标识集合）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoRespDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户脱敏公开资料 */
    private UserVo user;

    /** 用户持有的角色标识集合 (如 ["admin"] 或 ["common"]) */
    private Set<String> roles;

    /** 用户持有的按钮及操作权限字符集合 (如 ["vr:scene:add", "site:nav:list"]) */
    private Set<String> permissions;
}

