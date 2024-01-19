package com.wang.usercenter.common;

/**
 * @author wanglibin
 * @version 1.0
 */
public enum ErrorCode {
    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000,"params error",""),
    NULL_ERROR(40001,"data is null",""),
    NOT_LOGIN(40100,"not login",""),
    NO_AUTH(40101,"no authority",""),
    SYSTEM_ERROR(50000,"system error","")
    ;
    private final int code;
    /**
     * 错误码信息
     */
    private final String msg;
    /**
     * 详细描述
     */
    private final String decription;

    ErrorCode(int code, String msg, String decription) {
        this.code = code;
        this.msg = msg;
        this.decription = decription;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getDecription() {
        return decription;
    }
}
