package com.wang.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.usercenter.domain.UserTeam;
import com.wang.usercenter.service.UserTeamService;
import com.wang.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author wlbin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-03-08 20:57:40
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




