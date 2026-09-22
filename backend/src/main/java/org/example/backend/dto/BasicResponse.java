package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 全局统一接口响应结构 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicResponse<T> implements Serializable {

    /** 业务操作状态：true 成功，false 失败 */
    @Builder.Default
    private Boolean status = true;

    /** 状态码：200 成功，其他为业务/系统错误码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 实际返回的数据体 */
    private T data;

    // ================= 快捷静态构造方法 =================

    /**
     * 成功响应（带数据）
     */
    public static <T> BasicResponse<T> success(T data) {
        BasicResponse<T> basicResponse = new BasicResponse<>();
        basicResponse.setStatus(true);
        basicResponse.setCode(200);
        basicResponse.setMessage("操作成功");
        basicResponse.setData(data);
        return basicResponse;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> BasicResponse<T> success() {
        return success(null);
    }

    /**
     * 失败响应（默认 500 错误码）
     */
    public static <T> BasicResponse<T> error(String message) {
        BasicResponse<T> basicResponse = new BasicResponse<>();
        basicResponse.setStatus(false);
        basicResponse.setCode(500);
        basicResponse.setMessage(message);
        basicResponse.setData(null);
        return basicResponse;
    }

    /**
     * 失败响应（自定义错误码和提示）
     */
    public static <T> BasicResponse<T> error(Integer code, String message) {
        BasicResponse<T> basicResponse = new BasicResponse<>();
        basicResponse.setStatus(false);
        basicResponse.setCode(code);
        basicResponse.setMessage(message);
        basicResponse.setData(null);
        return basicResponse;
    }
}
