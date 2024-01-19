package com.wang.usercenter.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wanglibin
 * @version 1.0
 */
@Data
public class RegisterRequest implements Serializable {

    private String userAccount;
    private String userPwd;
    private String checkPwd;
    private String planetCode;
}
