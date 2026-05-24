package com.whtc.employee.exception;

import com.whtc.employee.common.Result;
import com.whtc.employee.constant.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public Result<String> exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常 - 不暴露具体数据库字段值
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库完整性约束异常：{}", ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {
            // 不暴露具体字段值，使用预定义错误消息
            return Result.error(MessageConstant.ACCOUNT_EXISTS);
        }
        return Result.error(MessageConstant.SYSTEM_ERROR);
    }

    /**
     * 处理404找不到处理器异常
     * @param e
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.error("请求路径不存在：{}", e.getRequestURL());
        return Result.error("请求路径不存在");
    }

    /**
     * 处理请求方法不支持异常
     * @param e
     * @return
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error("请求方法不支持：{}", e.getMessage());
        return Result.error("请求方法不支持");
    }

    /**
     * 处理请求参数缺失异常
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("缺少必要参数：{}", e.getParameterName());
        return Result.error("缺少必要参数：" + e.getParameterName());
    }

    /**
     * 处理请求体不可读异常
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("请求参数格式错误：{}", e.getMessage());
        return Result.error(MessageConstant.PARAM_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足：{}", e.getMessage());
        return Result.error(403, "无权限访问");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(MessageConstant.SYSTEM_ERROR);
    }

    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("参数绑定异常：{}", message);
        return Result.error(message);
    }

    /**
     * 处理JSR-303参数校验异常（@Valid）
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("参数校验异常：{}", message);
        return Result.error(message);
    }
}
