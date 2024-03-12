package com.wang.usercenter.domain.response;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.wang.usercenter.domain.User;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author wanglibin
 * @version 1.0
 */
@Data
public class TeamResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -8844086997739030793L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 队伍人员
     */
    private List<User> userList;

    /**
     * 已加入队伍人数
     */
    private Integer nums;
    /**
     * 是否是队伍成员
     */
    private Boolean hasJoin = false;
}
