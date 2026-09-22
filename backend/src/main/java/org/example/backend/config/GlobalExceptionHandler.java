package org.example.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.BizException;
import org.example.backend.dto.BasicResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 核心原理：任何地方（包括数据库、业务逻辑、框架底层）抛出的异常，都会被这里的 @ExceptionHandler 精准拦截并定制返回信息！
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 【第 1 处拦截】：专门拦截我们主动抛出的“业务提示”（如密码错误、用户不存在）
     */
    @ExceptionHandler(BizException.class)
    public BasicResponse<?> handleBizException(BizException e) {
        log.warn("【业务拦截提示】: {}", e.getMessage());
        return BasicResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 【参数校验拦截】：专门拦截 @Valid 参数校验失败异常 (Spring Validation)
     * 自动提取我们在 DTO 注解上写的友好 message（如 "分类名称不能为空"）
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public BasicResponse<?> handleMethodArgumentNotValidException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        org.springframework.validation.FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数不合法";
        log.warn("【参数校验失败】字段 [{}] 校验未通过: {}", fieldError != null ? fieldError.getField() : "unknown", message);
        return BasicResponse.error(400, message);
    }

    /**
     * 【第 2 处拦截】：专门拦截“数据库没开 / 连接超时”的异常！
     * 💡 就是在这里定制你想要的数据库故障中文文案！
     */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public BasicResponse<?> handleCannotGetJdbcConnectionException(CannotGetJdbcConnectionException e) {
        // 在后台控制台打印真实详细报错，方便程序员看
        log.error("【数据库连接失败】无法建立与 MySQL 的连接: ", e);
        
        // 👈 在这里写你要改成的任何自定义错误提示！
        return BasicResponse.error("数据库连接失败：本地 MySQL 服务未开启或连接超时，请检查 MySQL 服务！");
    }

    /**
     * 【第 3 处拦截】：专门拦截其它数据库执行异常（如 SQL 语法写错、字段不存在等）
     */
    @ExceptionHandler(DataAccessException.class)
    public BasicResponse<?> handleDataAccessException(DataAccessException e) {
        log.error("【数据库执行异常】: ", e);
        return BasicResponse.error("数据库操作异常，请检查 SQL 或数据表结构！");
    }

    /**
     * 【第 4 处拦截】：兜底拦截所有未知的系统崩溃（如空指针、除以0等），防止英文堆栈泄露
     */
    @ExceptionHandler(Exception.class)
    public BasicResponse<?> handleException(Exception e) {
        log.error("【系统未知异常】: ", e);
        return BasicResponse.error("系统开小差了，请稍后重试～");
    }
}
