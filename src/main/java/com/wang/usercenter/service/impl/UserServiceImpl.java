package com.wang.usercenter.service.impl;

import java.net.http.HttpRequest;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.usercenter.common.ErrorCode;
import com.wang.usercenter.common.ResultUtils;
import com.wang.usercenter.domain.User;
import com.wang.usercenter.domain.response.UserResponse;
import com.wang.usercenter.exception.BaseException;
import com.wang.usercenter.service.UserService;
import com.wang.usercenter.mapper.UserMapper;
import com.wang.usercenter.utils.AlgorithmUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.marshalling.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.wang.usercenter.content.UserContent.ADMIN_ROLE;
import static com.wang.usercenter.content.UserContent.USER_LOGIN_STATE;

/**
 * @author wlbin
 * @description 针对表【t_user(用户表)】的数据库操作Service实现
 * @createDate 2024-01-14 18:01:28
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    private static final String SALT = "wang";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public long userRegister(String userAccount, String userPwd, String checkPwd, String planetCode) {
        if (StringUtils.isAllBlank(userAccount, userPwd, checkPwd, planetCode)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "账户长度小于4");
        }
        if (userPwd.length() < 8 || checkPwd.length() < 8) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "密码长度小于8");
        }
        if (planetCode.length() > 6) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "星球编号大于6");
        }
        Pattern compile = Pattern.compile("[[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t]+");
        Matcher matcher = compile.matcher(userAccount);

        if (matcher.find()) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "账户包含特殊字符");
        }
        if (!userPwd.equals(checkPwd)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "密码不一致");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_count", userAccount);// userCount将报错
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "账户已存在");
        }
        wrapper = new QueryWrapper<>();
        wrapper.eq("planet_code", planetCode);
        long n = this.count(wrapper);
        if (n > 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "星球编号已使用");
        }
        String encrypt = DigestUtils.md5DigestAsHex((SALT + userPwd).getBytes());
        User user = new User();
        user.setUserCount(userAccount);
        user.setUserPwd(encrypt);
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if (!save) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPwd, HttpServletRequest request) {
        if (StringUtils.isAllBlank(userAccount, userPwd)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        if (userAccount.length() < 4) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "账户长度小于4");
        }
        if (userPwd.length() < 8) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "密码长度小于8");
        }
        Pattern compile = Pattern.compile("[[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t]+");
        Matcher matcher = compile.matcher(userAccount);

        if (matcher.find()) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "账户包含特殊字符");
        }
        String encrypt = DigestUtils.md5DigestAsHex((SALT + userPwd).getBytes());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_count", userAccount);// userCount将报错
        wrapper.eq("user_pwd", encrypt);
        User user = this.getOne(wrapper);
        if (user == null) {
            log.info("login failed, user_count and user_pwd not match");
            throw new BaseException(ErrorCode.PARAMS_ERROR, "", "账户不存在");
        }
        // 用户脱敏
        User newUser = getSafetyUser(user);
        //记录登录状态 现在使用redis 自动保存到redis
        request.getSession().setAttribute(USER_LOGIN_STATE, newUser);
        return newUser;
    }

    public User getSafetyUser(User user) {
        if (user == null) return null;
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
        newUser.setTags(user.getTags());
        newUser.setProfile(user.getProfile());
        return newUser;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }

    /**
     * sql 查询
     *
     * @param tagList
     * @return
     */
    @Deprecated
    public List<User> searchByTagsBySQL(List<String> tagList) {
        if (CollectionUtils.isEmpty(tagList)) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        for (String tag : tagList) {
            wrapper = wrapper.like("tags", tag);
        }
        List<User> list = this.list(wrapper);
        return list.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int update(User user, HttpServletRequest request) {
        if (user == null || request == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        // 鉴权
        User currentUser = getCurrentUser(request);
        if (!isAdmin(currentUser) && currentUser.getId() != user.getId()) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        User selectById = userMapper.selectById(user.getId());
        if (selectById == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user == null || user.getRole() != ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        return loginUser.getRole() == ADMIN_ROLE;
    }

    @Override
    public User getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        if (user == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    @Override
    public Page<User> getRecommendList(long pageNum, long pageSize, HttpServletRequest request) {
        User loginUser = getCurrentUser(request);
        String redisKey = String.format("user_center:user:recommend:" + loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> pageUser = (Page<User>) valueOperations.get(redisKey);
        if (pageUser != null) {
            return pageUser;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        Page<User> userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<User> collect = userPage.getRecords().stream().map(this::getSafetyUser).collect(Collectors.toList());
        Page<User> ret = userPage.setRecords(collect);
        try {
            valueOperations.set(redisKey, ret, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.info("/user/recommend redis set error", e);
        }
        return ret;
    }

    @Override
    public List<User> matchUsers(Long num, User loginUser) {
        String redisKey = String.format("user_center:user:match:" + loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        List<User> users = (List<User>) valueOperations.get(redisKey);
        if (users != null) {
            return users;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getB() - b.getB()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getA().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        try {
            valueOperations.set(redisKey, finalUserList, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.info("/user/match redis set error", e);
        }
        return finalUserList;
    }


    /**
     * 内存查询 - 查询所有用户在内存中判断
     *
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = this.list(queryWrapper);
        Gson gson = new Gson();
        //2.判断内存中是否包含要求的标签 parallelStream()
        //https://blog.csdn.net/weixin_44227650/article/details/123047880?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522166787335916782414989165%2522%252C%2522scm%2522%253A%252220140713.130102334..%2522%257D&request_id=166787335916782414989165&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~all~baidu_landing_v2~default-1-123047880-null-null.142^v63^control,201^v3^control_2,213^v1^t3_esquery_v2&utm_term=parallelStream%28%29&spm=1018.2226.3001.4187
        return userList.stream().filter(user -> {
            String tagstr = user.getTags();
//            if (StringUtils.isBlank(tagstr)){
//                return false;
//            }
            Set<String> tempTagNameSet = gson.fromJson(tagstr, new TypeToken<Set<String>>() {
            }.getType());
            //java8  Optional 来判断空
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());

            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }
}




