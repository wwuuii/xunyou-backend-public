package com.yuxian.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.constant.RedisConstant;
import com.yuxian.yupao.enums.TeamStatusEnum;
import com.yuxian.yupao.exception.BusinessException;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.model.entity.Team;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.entity.UserTeam;
import com.yuxian.yupao.service.UserTeamService;
import com.yuxian.yupao.mapper.UserTeamMapper;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author admin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-07-09 16:56:04
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{


	@Override
	public boolean addUserTeam(Long userId, Long teamId) {
		checkAdd(userId, teamId);
		UserTeam userTeam = new UserTeam();
		userTeam.setUserId(userId);
		userTeam.setTeamId(teamId);
		userTeam.setJoinTime(new Date());
		return this.save(userTeam);
	}

	@Override
	public List<Long> queryJoinTeamUserIds(Long teamId) {
		ThrowUtils.throwIf(teamId == null,ErrorCode.PARAMS_ERROR, "队伍Id不能为空");
		List<UserTeam> userTeams = Optional.of(query().eq("teamId", teamId).list()).orElse(new ArrayList<>());
		return userTeams.stream().map(UserTeam::getUserId).collect(Collectors.toList());
	}

	private void checkAdd(Long userId, Long teamId) {
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("userId", userId);
		long hasJoinNum = count(userTeamQueryWrapper);
		if (hasJoinNum > 5) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入5个队伍");
		}
		//不能重复加入已加入的队伍
		userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("userId", userId);
		userTeamQueryWrapper.eq("teamId", teamId);
		long hasUserJoinTeam = count(userTeamQueryWrapper);
		if (hasUserJoinTeam > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
		}
	}
}




