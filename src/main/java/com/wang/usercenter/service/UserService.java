package com.wang.usercenter.service;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.usercenter.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.usercenter.domain.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.http.HttpRequest;
import java.util.List;

/**
* @author wlbin
* @description 针对表【t_user(用户表)】的数据库操作Service
* @createDate 2024-01-14 18:01:28
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     * @param userAccount 账户
     * @param userPwd 密码
     * @param checkPwd 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPwd, String checkPwd,String planetCode);

    /**
     * 用户登录
     * @param userAccount 账户
     * @param userPwd 密码
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPwd, HttpServletRequest request);

    /**
     * 脱敏
     * @param user 用户信息
     * @return User
     */
    public User getSafetyUser(User user);

    /**
     * 用户注销
     * @param request
     */
    public void userLogout(HttpServletRequest request);

    /**
     * 根据标签查找用户 内存
     * @param tagList
     * @return
     */
    public List<User> searchByTags(List<String> tagList);

    /**
     * 根据标签查找用户 sql
     * @param tagList
     * @return
     */
    public List<User> searchByTagsBySQL(List<String> tagList);

    /**
     * 用户更新信息
     * @param user
     * @param request
     * @return
     */
    int update(User user, HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 获取当前用户
     * @param request
     * @return
     */
    User getCurrentUser(HttpServletRequest request);

    Page<User> getRecommendList(long pageNum, long pageSize, HttpServletRequest request);

    List<User> matchUsers(Long num, User loginUser);

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
}
