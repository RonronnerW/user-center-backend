package com.wang.usercenter.common;

/**
 * @author wanglibin
 * @version 1.0
 */
public class ResultUtils {
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data, "ok", "");
    }
    public static <T> BaseResponse<T> set(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }
    public static <T> BaseResponse<T> set(int code, String msg, String description) {
        return new BaseResponse<>(code, null, msg, description);
    }
}
