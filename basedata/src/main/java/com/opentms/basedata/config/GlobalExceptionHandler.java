package com.opentms.basedata.config;

import com.opentms.common.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.core.convert.ConversionException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.BindException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public Result<Void> handleTypeMismatch(TypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        return Result.badRequest("ID参数格式不正确");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不匹配: {}", e.getMessage());
        return Result.badRequest("ID参数格式不正确");
    }

    @ExceptionHandler(ConversionException.class)
    public Result<Void> handleConversion(ConversionException e) {
        log.warn("类型转换错误: {}", e.getMessage());
        return Result.badRequest("ID参数格式不正确");
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        log.warn("绑定错误: {}", e.getMessage());
        return Result.badRequest("ID参数格式不正确");
    }

    @ExceptionHandler(NumberFormatException.class)
    public Result<Void> handleNumberFormat(NumberFormatException e) {
        log.warn("数字格式错误: {}", e.getMessage());
        return Result.badRequest("ID参数格式不正确");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        if (e.getMessage() != null && e.getMessage().contains("Failed to convert")) {
            return Result.badRequest("ID参数格式不正确");
        }
        log.warn("参数非法: {}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        String msg = e.getMessage();
        if (msg != null) {
            if (msg.contains("Failed to convert") || msg.contains("could not be converted")) {
                log.warn("类型转换错误: {}", msg);
                return Result.badRequest("ID参数格式不正确");
            }
        }
        log.error("系统异常: {}", msg, e);
        return Result.error("系统异常: " + msg);
    }
}