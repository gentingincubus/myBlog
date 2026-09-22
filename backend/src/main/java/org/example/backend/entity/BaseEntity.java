package org.example.backend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用实体基类（企业级规范）
 * 集中管理雪花 ID、审计字段（创建人、创建时间、更新时间）以及逻辑删除
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID（使用雪花算法 Snowflake 自动生成 19 位全局唯一 Long 型整数）
     * 避免使用自增 ID 带来的规律遍历风险与分布式分库分表冲突
     */
    @TableId(type = IdType.ASSIGN_ID)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long id;

    /**
     * 创建人用户 ID（插入记录时通过 MetaObjectHandler 自动注入当前登录人）
     */
    @TableField(fill = FieldFill.INSERT)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.ToStringSerializer.class)
    private Long createBy;

    /**
     * 创建时间（插入记录时通过 MetaObjectHandler 自动注入当前时间）
     */
    @TableField(fill = FieldFill.INSERT)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间（插入和每次更新记录时自动更新为当前时间）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志位（0: 正常, 1: 已删除）
     * 打上 @TableLogic 后：
     * - 执行 removeById 时自动变为 UPDATE deleted = 1
     * - 执行 selectList 时自动附加 WHERE deleted = 0
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
