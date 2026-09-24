package org.example.backend.dto.vr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VR 全景场景查询入参 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VrSceneQueryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属分类ID (可选，若不传则查所有或结合名称筛选)
     */
    private Long categoryId;

    /**
     * 场景名称模糊查询
     */
    private String name;

    /**
     * 状态筛选 (0: 禁用, 1: 启用)
     */
    private Integer status;
}

