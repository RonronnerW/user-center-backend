package com.wang.usercenter.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wanglibin
 * @version 1.0
 */
@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 8710575458335558810L;
    private int pageSize = 10;
    private int pageNum = 1;
}
