package com.wang.usercenter.service.impl;
import java.net.http.HttpRequest;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.usercenter.common.ErrorCode;
import com.wang.usercenter.domain.User;
import com.wang.usercenter.exception.BaseException;
import com.wang.usercenter.service.UserService;
import com.wang.usercenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wang.usercenter.content.UserContent.USER_LOGIN_STATE;

/**
* @author wlbin
* @description 针对表【t_user(用户表)】的数据库操作Service实现
* @createDate 2024-01-14 18:01:28
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    private static final String SALT  = "wang";

    @Override
    public long userRegister(String userAccount, String userPwd, String checkPwd, String planetCode) {
        if(StringUtils.isAllBlank(userAccount, userPwd,checkPwd, planetCode)) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR);
        }
        if(userAccount.length()<4) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","账户长度小于4");
        }
        if(userPwd.length()<8 || checkPwd.length()<8) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","密码长度小于8");
        }
        if(planetCode.length()>6) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","星球编号大于6");
        }
        Pattern compile = Pattern.compile("[[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t]+");
        Matcher matcher = compile.matcher(userAccount);

        if(matcher.find()) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","账户包含特殊字符");
        }
        if(!userPwd.equals(checkPwd)) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","密码不一致");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_count",userAccount);// userCount将报错
        long count = this.count(wrapper);
        if(count>0) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","账户已存在");
        }
        wrapper = new QueryWrapper<>();
        wrapper.eq("planet_code", planetCode);
        long n = this.count(wrapper);
        if(n>0) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","星球编号已使用");
        }
        String encrypt = DigestUtils.md5DigestAsHex((SALT + userPwd).getBytes());
        User user = new User();
        user.setUserCount(userAccount);
        user.setUserPwd(encrypt);
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if(!save) {
            throw  new BaseException(ErrorCode.NULL_ERROR);
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPwd, HttpServletRequest request) {
        if(StringUtils.isAllBlank(userAccount, userPwd)) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR);
        }
        if(userAccount.length()<4) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","账户长度小于4");
        }
        if(userPwd.length()<8) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","密码长度小于8");
        }
        Pattern compile = Pattern.compile("[[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t]+");
        Matcher matcher = compile.matcher(userAccount);

        if(matcher.find()) {
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","账户包含特殊字符");
        }
        String encrypt = DigestUtils.md5DigestAsHex((SALT + userPwd).getBytes());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_count",userAccount);// userCount将报错
        wrapper.eq("user_pwd",encrypt);
        User user = this.getOne(wrapper);
        if(user==null) {
            log.info("login failed, user_count and user_pwd not match");
            throw  new BaseException(ErrorCode.PARAMS_ERROR,"","账户不存在");
        }
        // 用户脱敏
        User newUser = getSafetyUser(user);
        //记录登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, newUser);

        return newUser;
    }
    public User getSafetyUser(User user) {
        if(user==null) return null;
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setUsername(user.getUsername());
        newUser.setUserCount(user.getUserCount());
        newUser.setAvatarUrl(user.getAvatarUrl());
        newUser.setGender(user.getGender());
        newUser.setEmail(user.getEmail());
        newUser.setRole(user.getRole());
        newUser.setUserStatus(user.getUserStatus());
        newUser.setPhone(user.getPhone());
        newUser.setCreateTime(user.getCreateTime());
        newUser.setPlanetCode(user.getPlanetCode());
        return newUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }
}




