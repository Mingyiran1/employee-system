package com.whtc.employee.exception;

/**
 * 业务异常
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }

    /**
     * 支持异常链的构造函数
     * @param msg 异常消息
     * @param cause 原始异常
     */
    public BaseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * 支持异常链的构造函数
     * @param cause 原始异常
     */
    public BaseException(Throwable cause) {
        super(cause);
    }

}