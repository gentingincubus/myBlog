package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 系统角色实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称 (如: 超级管理员、普通用户) */
    private String roleName;

    /** 角色权限字符 (如: admin、common) */
    private String roleKey;

    /** 显示顺序 */
    private Integer sort;

    /** 角色状态 (1: 正常, 0: 停用) */
    private Integer status;

    /** 备注说明 */
    private String remark;
}

