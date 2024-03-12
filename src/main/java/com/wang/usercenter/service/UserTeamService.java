package com.wang.usercenter.service;

import com.wang.usercenter.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.usercenter.mapper.UserTeamMapper;

/**
* @author wlbin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2024-03-08 20:57:40
*/
public interface UserTeamService extends IService<UserTeam> {
    UserTeam userTeam = new UserTeam();
}
