package org.example.backend.annotation;

/**
 * 权限逻辑运算枚举
 */
public enum Logical {
    /** 必须同时具备所有指定权限 */
    AND,
    /** 只需具备其中任意一个权限即可 */
    OR
}

