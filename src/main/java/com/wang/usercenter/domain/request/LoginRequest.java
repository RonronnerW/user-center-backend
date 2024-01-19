package com.wang.usercenter.domain.request;

import lombok.Data;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author wanglibin
 * @version 1.0
 */
@Data
public class LoginRequest {
    private String userAccount;
    private String userPwd;
}
