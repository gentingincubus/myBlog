package org.example.backend.dto.vr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VR 全景分类查询入参 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrCategoryQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称模糊查询
     */
    private String name;

    /**
     * 分类英文标识
     */
    private String code;

    /**
     * 状态筛选 (0: 禁用, 1: 启用)
     */
    private Integer status;
}

