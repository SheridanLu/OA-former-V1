package com.mochu.framework.config;

import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        List<R.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new R.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return R.fail(400, "参数校验失败", errors);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBind(BindException e) {
        List<R.FieldError> errors = e.getFieldErrors().stream()
                .map(fe -> new R.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return R.fail(400, "参数校验失败", errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(AccessDeniedException e) {
        return R.fail(HttpStatus.FORBIDDEN.value(), "无权限访问");
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleGeneric(Exception e) {
        log.error("系统异常", e);
        return R.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误");
    }
}
