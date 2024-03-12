package com.wang.usercenter.service.impl;
import java.util.ArrayList;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.usercenter.common.ErrorCode;
import com.wang.usercenter.domain.Team;
import com.wang.usercenter.domain.User;
import com.wang.usercenter.domain.UserTeam;
import com.wang.usercenter.domain.request.TeamQueryRequest;
import com.wang.usercenter.domain.response.TeamResponse;
import com.wang.usercenter.domain.response.UserResponse;
import com.wang.usercenter.enums.TeamStatusEnum;
import com.wang.usercenter.exception.BaseException;
import com.wang.usercenter.mapper.UserTeamMapper;
import com.wang.usercenter.service.TeamService;
import com.wang.usercenter.mapper.TeamMapper;
import com.wang.usercenter.service.UserService;
import com.wang.usercenter.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* @author wlbin
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-03-08 20:57:40
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private UserTeamService userTeamService;

    @Autowired
    private UserTeamMapper userTeamMapper;

    @Autowired
    private UserService userService;

    @Transactional(rollbackFor = Exception.class) // 事务注解
    @Override
    public Long addTeam(Team team) {
        if(team==null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Integer maxNum = team.getMaxNum();
        if (maxNum < 1 || maxNum > 20){
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍人数不符合要求");
        }
        //  b. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //  c. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
        }
        //  d. status 是否公开（int）不传默认为 0（公开）
        int status = team.getStatus();
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //  e. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BaseException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //  f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }

        //  g. 校验用户最多创建 5 个队伍
        // todo 有 bug，可能同时创建 100 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",team.getUserId());
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表 插入队伍并获取数据库中的id
        team.setId(null);
        int result = teamMapper.insert(team);

        Long teamId = team.getId();
        if (result<0 || teamId == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(team.getUserId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);
        if (!save) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamResponse> query(TeamQueryRequest teamQueryRequest, User loginUser) {

        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if(teamQueryRequest!=null) {

            Long id = teamQueryRequest.getId();
            if(id!=null && id>=0) {
                teamQueryWrapper.eq("id", id);
            }
            String name = teamQueryRequest.getName();
            if(StringUtils.isNotBlank(name)) {
                teamQueryWrapper.like("name", name);
            }
            String description = teamQueryRequest.getDescription();
            if(StringUtils.isNotBlank(description)) {
                teamQueryWrapper.like("description", description);
            }
            String searchText = teamQueryRequest.getSearchText();
            if(StringUtils.isNotBlank(searchText)) {
                teamQueryWrapper.like("name", searchText)
                        .or().like("description", searchText);
            }
            Long userId = teamQueryRequest.getUserId();
            if(userId!=null && userId>=0) {
                teamQueryWrapper.eq("user_id", userId);
            }
            Integer status = teamQueryRequest.getStatus();
            if(status!=null && status>=0 && status<3) {
                teamQueryWrapper.eq("status", status);
            }
        }
        // 查询所有队伍
        List<Team> teams = this.list(teamQueryWrapper);
        if(CollectionUtils.isEmpty(teams)) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        // 不展示过期的队伍
        teams.stream().map(team -> team.getExpireTime().after(new Date())).collect(Collectors.toList());

        // 判断是否是管理员
        boolean admin = userService.isAdmin(loginUser);
        // 非管理员只能查看公开队伍和加密队伍
        if(!admin) {
            teams.stream().map(team -> team.getStatus()!=1).collect(Collectors.toList());
        }

        ArrayList<TeamResponse> teamResponses = new ArrayList<>();//返回值
        for (Team team : teams) {
            List<User> users = getUsersByTeamId(team.getId());
            List<Long> userIds = users.stream().map(User::getId).toList();
            TeamResponse teamResponse = new TeamResponse();
            BeanUtils.copyProperties(team, teamResponse);
            teamResponse.setUserList(users);
            teamResponse.setNums(users.size());
            if(Objects.equals(team.getUserId(), loginUser.getId()) || userIds.contains(loginUser.getId())) {
                teamResponse.setHasJoin(true);
            }
            teamResponses.add(teamResponse);
        }
        return teamResponses;
    }

    @Override
    public Long upfateTeam(Team team, HttpServletRequest request) {
        if(team==null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = team.getId();
        // 队伍是否存在
        Team oldTeam = teamMapper.selectById(teamId);
        if(oldTeam==null) {
            throw new BaseException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 管理员或者队伍创建者可以修改队伍信息
        Long userId = oldTeam.getUserId();

        // 创建队伍的用户
        User user = userService.getById(userId);
        boolean admin = userService.isAdmin(user);
        // 获取当前用户
        User currentUser = userService.getCurrentUser(request);
        if(!admin && !currentUser.getId().equals(userId)) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        int update = teamMapper.updateById(team);

        return team.getId();
    }

    @Override
    public boolean joinTeam(String password, Long teamId, HttpServletRequest request) {
        Team team = teamMapper.selectById(teamId);
        if(team==null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        Date expireTime = team.getExpireTime();
        if(expireTime.before(new Date())) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer maxNum = team.getMaxNum();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count>=maxNum) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "队伍人已满");
        }
        // 不能加入自己队伍，和已加入队伍
        Long userId = userService.getCurrentUser(request).getId();
        if(userId.equals(team.getUserId())) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"不能加入自己队伍");
        }
        userTeamQueryWrapper.eq("user_id", userId);
        long cnt = userTeamService.count(userTeamQueryWrapper);
        if(cnt>0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"不能重复加入");
        }
        if (team.getStatus()==1) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"不能加入私有队伍");
        }
        if(team.getStatus()==2 && (password==null || !password.equals(team.getPassword()))) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"加入密码错误");
        }
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务注解
    public boolean quitTeam(Long teamId, HttpServletRequest request) {
        if(teamId==null || teamId<=0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        // 判断队伍是否存在
        Team team = this.getById(teamId);
        if(team==null) {
            throw new BaseException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        // 是否加入队伍
        Long userId = currentUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        long nums = userTeamService.count(userTeamQueryWrapper);
        userTeamQueryWrapper.eq("user_id", userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count<=0) {
            throw new BaseException(ErrorCode.NULL_ERROR,"未加入队伍");
        }
        // 一人直接删除队伍和用户队伍记录
        if(nums==1) {
            teamMapper.deleteById(teamId);
        } else {
            // 是创建者则转让给最早加入队伍的
            if(team.getUserId().equals(userId)) {
                List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
                list.stream().sorted((o1, o2) -> o1.getJoinTime().compareTo(o2.getJoinTime())).collect(Collectors.toList());
                team.setUserId(list.get(0).getId());
                int i = teamMapper.updateById(team);
                if(i<=0) {
                    throw new BaseException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
                }
            }
        }
        return userTeamService.remove(userTeamQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务注解
    public boolean deleteTeam(Long teamId, HttpServletRequest request) {
        if(teamId==null || teamId<=0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getCurrentUser(request);
        Long userId = currentUser.getId();
        Team team = teamMapper.selectById(teamId);
        if(team==null) {
            throw new BaseException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if(!team.getUserId().equals(userId) && userService.isAdmin(currentUser)) {
            throw new BaseException(ErrorCode.NO_AUTH,"无权限删除队伍");
        }
        // 删除队伍和用户-队伍关系表
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        userTeamQueryWrapper.eq("user_id", userId);
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        if(!remove) {
            throw new BaseException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return this.removeById(teamId);
    }

    @Override
    public List<TeamResponse> getJoinTeams(Long userId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id", userId);
        List<UserTeam> list = userTeamService.list(userTeamQueryWrapper);
        if(list==null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        List<Long> teamList = list.stream().map(UserTeam::getTeamId).collect(Collectors.toList());
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.in("id", teamList);
        List<Team> teams = teamMapper.selectList(teamQueryWrapper);
        ArrayList<TeamResponse> teamResponses = new ArrayList<>();
        for (Team team : teams) {
            TeamResponse teamResponse = new TeamResponse();
            BeanUtils.copyProperties(team, teamResponse);
            teamResponses.add(teamResponse);
        }
        return teamResponses;
    }

    @Override
    public List<User> getUsersByTeamId(Long teamId) {
        if(teamId == null || teamId<=0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 查询队伍-用户表，找到加入队伍的用户id
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.in("team_id", teamId);
        List<UserTeam> userTeams = userTeamService.list(userTeamQueryWrapper);
        if(userTeams==null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        List<Long> userIds = userTeams.stream().map(UserTeam::getUserId).distinct().toList();
        // 2. 根据用户id查详细用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIds);
        List<User> users = userService.list(userQueryWrapper);
        users.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        return users;
    }
}




