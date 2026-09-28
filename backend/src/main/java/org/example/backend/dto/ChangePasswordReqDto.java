package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求入参 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordReqDto {

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码（防误输）
     */
    private String confirmPassword;
}
