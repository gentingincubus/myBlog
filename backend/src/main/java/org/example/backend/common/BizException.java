package org.example.backend.common;

/**
 * 自定义业务异常类
 * 用于在 Service 业务逻辑中主动抛出可预期的、友好的中文业务错误提示（如：用户名已存在、密码错误等）
 */
public class BizException extends RuntimeException {

    /** 业务错误码，默认为 500 */
    private Integer code = 500;

    public BizException(String message) {
        super(message);
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
