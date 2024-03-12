package com.wang.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.usercenter.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.usercenter.domain.User;
import com.wang.usercenter.domain.request.TeamQueryRequest;
import com.wang.usercenter.domain.response.TeamResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author wlbin
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-03-08 20:57:40
*/
public interface TeamService extends IService<Team> {

    /**
     * 添加队伍
     * @param team
     * @return
     */
    Long addTeam(Team team);

    /**
     * 查询队伍列表
     * @param teamQueryRequest
     * @return
     */
    List<TeamResponse> query(TeamQueryRequest teamQueryRequest, User user);

    /**
     * 修改队伍信息
     * @param team
     * @param request
     * @return
     */
    Long upfateTeam(Team team, HttpServletRequest request);

    /**
     * 加入队伍
     * @param password
     * @param teamId
     * @param request
     * @return
     */
    boolean joinTeam(String password, Long teamId, HttpServletRequest request);

    /**
     * 退出队伍
     * @param teamId
     * @param request
     * @return
     */
    boolean quitTeam(Long teamId, HttpServletRequest request);

    /**
     * 删除队伍
     * @param teamId
     * @param request
     * @return
     */
    boolean deleteTeam(Long teamId, HttpServletRequest request);

    /**
     * 加入的队伍
     * @param userId
     * @return
     */
    List<TeamResponse> getJoinTeams(Long userId);

    List<User> getUsersByTeamId(Long teamId);
}
