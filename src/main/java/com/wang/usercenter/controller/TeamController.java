package com.wang.usercenter.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.usercenter.common.BaseResponse;
import com.wang.usercenter.common.ErrorCode;
import com.wang.usercenter.common.ResultUtils;
import com.wang.usercenter.domain.Team;
import com.wang.usercenter.domain.User;
import com.wang.usercenter.domain.request.TeamAddRequest;
import com.wang.usercenter.domain.request.TeamQueryRequest;
import com.wang.usercenter.domain.response.TeamResponse;
import com.wang.usercenter.exception.BaseException;
import com.wang.usercenter.service.TeamService;
import com.wang.usercenter.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wanglibin
 * @version 1.0
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http:localhost:3000", allowCredentials = "true")
@Slf4j
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    /**
     * 添加队伍
     * @param teamRequest
     * @param request
     * @return
     */
    @PostMapping ("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamRequest, HttpServletRequest request) {
        if(teamRequest==null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        if(currentUser==null) {
            throw new BaseException(ErrorCode.NOT_LOGIN);
        }
        System.out.println(teamRequest);
        Team team = new Team();
        BeanUtils.copyProperties(teamRequest, team);
        team.setUserId(currentUser.getId());
        System.out.println(team);
        Long teamId = teamService.addTeam(team);

        return ResultUtils.success(teamId);
    }

    /**
     * 查询队伍列表
     * @param teamQueryRequest
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<List<TeamResponse>> query(@RequestBody TeamQueryRequest teamQueryRequest, HttpServletRequest request) {
        if(teamQueryRequest ==null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        List<TeamResponse> teams = teamService.query(teamQueryRequest, currentUser);
        return ResultUtils.success(teams);
    }


    @PostMapping("/update")
    public BaseResponse<Long> update(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if(teamAddRequest==null) {
           throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        Long teamId = teamService.upfateTeam(team, request);
        return ResultUtils.success(teamId);
    }
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestParam(value = "password", required = false) String password, @RequestParam("teamId") Long teamId, HttpServletRequest request) {
        if(teamId<=0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        boolean ret = teamService.joinTeam(password, teamId, request);
        return ResultUtils.success(ret);
    }
    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestParam("teamId")Long teamId, HttpServletRequest request) {
        if(teamId==null || teamId<=0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        boolean ret = teamService.quitTeam(teamId, request);
        return ResultUtils.success(ret);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestParam("teamId")Long teamId, HttpServletRequest request) {
        if(teamId==null || teamId<=0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        boolean ret = teamService.deleteTeam(teamId, request);
        return ResultUtils.success(ret);
    }
    @GetMapping("/getJoinTeams")
    public BaseResponse<List<TeamResponse>> getJoinTeams(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        Long userId = currentUser.getId();
        List<TeamResponse> teamList = teamService.getJoinTeams(userId);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取创建的队伍
     * @param request
     * @return
     */
    @GetMapping("/getBuildTeam")
    public BaseResponse<List<TeamResponse>> getBuildTeam(HttpServletRequest request) {
        User currentUser = userService.getCurrentUser(request);
        TeamQueryRequest teamQueryRequest = new TeamQueryRequest();
        teamQueryRequest.setUserId(currentUser.getId());
        return query(teamQueryRequest, request);
    }
    @GetMapping("/get")
    public BaseResponse<TeamResponse> getTeamById(@RequestParam("teamId") Long teamId) {
        if(teamId==null || teamId<=0 ) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(teamId);
        TeamResponse teamResponse = new TeamResponse();
        BeanUtils.copyProperties(team,teamResponse);
        return ResultUtils.success(teamResponse);
    }

}
