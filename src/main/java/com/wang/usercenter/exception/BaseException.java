package com.wang.usercenter.exception;

import com.wang.usercenter.common.ErrorCode;

/**
 * @author wanglibin
 * @version 1.0
 */
public class BaseException extends RuntimeException{
    private final int code;
    private final String msg;
    private final String description;

    public BaseException(int code, String msg, String description) {
        this.code = code;
        this.msg = msg;
        this.description = description;
    }
    public BaseException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
        this.description = errorCode.getDecription();
    }
    public BaseException(ErrorCode errorCode, String msg) {
        this.code = errorCode.getCode();
        this.msg = msg;
        this.description = errorCode.getDecription();
    }
    public BaseException(ErrorCode errorCode, String msg, String description) {
        this.code = errorCode.getCode();
        this.msg = msg;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}
