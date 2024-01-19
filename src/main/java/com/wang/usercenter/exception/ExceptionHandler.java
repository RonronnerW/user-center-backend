package com.wang.usercenter.exception;

import com.wang.usercenter.common.BaseResponse;
import com.wang.usercenter.common.ErrorCode;
import com.wang.usercenter.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author wanglibin
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(BaseException.class)
    public BaseResponse<Object> BaseExceptionHandler(BaseException e) {
        log.error("BaseException: " + e);
        log.info(e.getCode()+" - "+e.getMsg()+" - "+e.getDescription());
        return ResultUtils.set(e.getCode(),e.getMsg(),e.getDescription());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public BaseResponse<Object> RuntimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException: " + e);
        return ResultUtils.set(ErrorCode.SYSTEM_ERROR);
    }
}
