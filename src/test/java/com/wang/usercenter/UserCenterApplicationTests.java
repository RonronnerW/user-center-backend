package com.wang.usercenter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.wang.usercenter.domain.User;
import com.wang.usercenter.mapper.UserMapper;
import com.wang.usercenter.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
class UserCenterApplicationTests {

    @Autowired
    UserMapper userMapper;

    @Resource
    UserService userService;

    @Test
    void contextLoads() {

        User user = new User();
        user.setId(0L);
        user.setUsername("wang");
        user.setUserCount("123");
        user.setAvatarUrl("");
        user.setGender(0);
        user.setUserPwd("xxx");
        user.setEmail("");
        user.setUserStatus(0);
        user.setPhone("");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);


        userService.save(user);

        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        userList.forEach(System.out::println);
    }

    @Test
    void RegisterTest() {
        String userCount = "";
        String userPwd = "12345678";
        String checkPwd = "12345678";
        String planetCode = "1";
        long l = userService.userRegister(userCount, userPwd, checkPwd, planetCode);
        Assertions.assertEquals(-1, l);

        userCount = "wan";
        userPwd = "12345678";
        checkPwd = "12345678";
        l = userService.userRegister(userCount, userPwd, checkPwd, planetCode);
        Assertions.assertEquals(-1, l);

        userCount = "wangl";
        userPwd = "123456";
        checkPwd = "12345678";
        l = userService.userRegister(userCount, userPwd, checkPwd, planetCode);
        Assertions.assertEquals(-1, l);

        userCount = "wang-@;";
        userPwd = "12345678";
        checkPwd = "12345678";
        l = userService.userRegister(userCount, userPwd, checkPwd, planetCode);
        Assertions.assertEquals(-1, l);

        userCount = "wang";
        userPwd = "1234567";
        checkPwd = "12345678";
        l = userService.userRegister(userCount, userPwd, checkPwd,planetCode);
        Assertions.assertEquals(-1, l);

        userCount = "wangl";
        userPwd = "12345678";
        checkPwd = "12345678";
        l = userService.userRegister(userCount, userPwd, checkPwd,planetCode);
        Assertions.assertEquals(8, l);

        userCount = "wangl";
        userPwd = "12345678";
        checkPwd = "12345678";
        l = userService.userRegister(userCount, userPwd, checkPwd,planetCode);
        Assertions.assertEquals(-1, l);

    }
    @Test
    void TestTags() {
        List<String> list = Arrays.asList("java");
        List<User> users = userService.searchByTags(list);
        for (User user : users) {
            System.out.println(user);
        };
    }
    @Test
    void TestTagsBySQL() {
        List<String> list = Arrays.asList("java");
        List<User> users = userService.searchByTagsBySQL(list);
        for (User user : users) {
            System.out.println(user);
        };
    }
}
