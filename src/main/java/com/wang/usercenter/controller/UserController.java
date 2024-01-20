package com.wang.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.usercenter.common.BaseResponse;
import com.wang.usercenter.common.ErrorCode;
import com.wang.usercenter.common.ResultUtils;
import com.wang.usercenter.domain.User;
import com.wang.usercenter.domain.request.LoginRequest;
import com.wang.usercenter.domain.request.RegisterRequest;
import com.wang.usercenter.exception.BaseException;
import com.wang.usercenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.wang.usercenter.content.UserContent.ADMIN_ROLE;
import static com.wang.usercenter.content.UserContent.USER_LOGIN_STATE;

/**
 * @author wanglibin
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody RegisterRequest userRequest) {
        if (userRequest == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userRequest.getUserAccount();
        String userPwd = userRequest.getUserPwd();
        String checkPwd = userRequest.getCheckPwd();
        String planetCode = userRequest.getPlanetCode();
        if (StringUtils.isAllBlank(userAccount, userPwd, checkPwd, planetCode)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        long ret = userService.userRegister(userAccount, userPwd, checkPwd, planetCode);
        return ResultUtils.success(ret);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }

        String userAccount = loginRequest.getUserAccount();
        String userPwd = loginRequest.getUserPwd();
        if (StringUtils.isAllBlank(userAccount, userPwd)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPwd, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        userService.userLogout(request);
        return ResultUtils.success(true);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> userList(String username, HttpServletRequest request) {
        // 鉴权 仅管理员可查询
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getRole() != ADMIN_ROLE) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            wrapper.like("username", username);
        }
        List<User> list = userService.list(wrapper);
        List<User> ret = list.stream().map(user1 -> {
            return userService.getSafetyUser(user1);
        }).collect(Collectors.toList());
        return ResultUtils.success(ret);

    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteList(@RequestBody long id, HttpServletRequest request) {
        // 鉴权 仅管理员可查询
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null || user.getRole() != ADMIN_ROLE) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        boolean ret = userService.removeById(id);
        return ResultUtils.success(ret);
    }

    /**
     * 获取当前用户状态
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> current(HttpServletRequest request) {

        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        Long id = user.getId();
        User us = userService.getById(id);
        User safetyUser = userService.getSafetyUser(us);
        return ResultUtils.success(safetyUser);
    }
}
