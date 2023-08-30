package com.yuxian.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.constant.RedisConstant;
import com.yuxian.yupao.enums.TeamStatusEnum;
import com.yuxian.yupao.exception.BusinessException;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.mapper.TeamMapper;
import com.yuxian.yupao.model.dto.team.*;
import com.yuxian.yupao.model.entity.Team;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.entity.UserTeam;
import com.yuxian.yupao.model.vo.team.TeamUserVO;
import com.yuxian.yupao.model.vo.user.UserVO;
import com.yuxian.yupao.service.TeamService;
import com.yuxian.yupao.service.UserService;
import com.yuxian.yupao.service.UserTeamService;
import com.yuxian.yupao.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author admin
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-07-09 16:53:54
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
		implements TeamService {
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UserService userService;
	@Resource
	private RedissonClient redissonClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public long addTeam(TeamAddRequest teamAddRequest, User loginUser) {
		final long userId = loginUser.getId();
		//检验信息
		checkTeamInfo(teamAddRequest);
		//插入队伍消息到队伍表
		RLock lock = redissonClient.getLock(RedisConstant.TEAM_CREATE_KEY + userId);
		Long teamId;
		//7.校验用户最多创建5个队伍
		try {
			if (lock.tryLock(1, TimeUnit.SECONDS)) {
				QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("userId", userId);
				long hasTeamNum = this.count(queryWrapper);
				if (hasTeamNum >= 5) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5个队伍");
				}
				Team team = new Team();
				BeanUtils.copyProperties(teamAddRequest, team);
				team.setUserId(userId);
				boolean result = this.save(team);
				teamId = team.getId();
				if (!result || teamId == null) {
					throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
				}
			} else {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "当前系统繁忙，请稍后再试！");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			lock.unlock();
		}
		boolean result = userTeamService.addUserTeam(userId, teamId);
		if (!result) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
		}
		return teamId;
	}

	@Override
	public List<TeamUserVO> searchTeam(TeamQuery teamQuery, boolean isAdmin) {
		QueryWrapper<Team> queryWrapper = getQueryCondition(teamQuery, isAdmin);

		//不展示已过期的队伍
		//expireTime is null or expireTime > now()
		queryWrapper.and(qw -> qw.gt("expireTime", new Date())).isNotNull("expireTime");

		List<Team> teamList = this.list(queryWrapper);
		if (CollectionUtils.isEmpty(teamList)) {
			return new ArrayList<>();
		}
		List<TeamUserVO> result = new ArrayList<>();
		//关联查询创建人的用户信息
		teamToTeamUserVO(teamList, result);
		return result;
	}

	/**
	 * team转换为teamVO
	 *
	 * @param teamList
	 * @param result
	 */
	private void teamToTeamUserVO(List<Team> teamList, List<TeamUserVO> result) {
		for (Team team : teamList) {
			Long userId = team.getUserId();
			Long teamId = team.getId();
			ThrowUtils.throwIf(userId == null || teamId == null, ErrorCode.SYSTEM_ERROR, "队伍数据错误");
			User user = userService.getById(userId);
			TeamUserVO teamUserVO = new TeamUserVO();
			BeanUtils.copyProperties(team, teamUserVO);
			//脱敏用户信息
			if (user != null) {
				UserVO userVO = new UserVO();
				BeanUtils.copyProperties(user, userVO);
				teamUserVO.setCreateUser(userVO);
			}
			List<Long> userIds = userTeamService.queryJoinTeamUserIds(teamId);
			teamUserVO.setUserIds(userIds);
			teamUserVO.setHasJoinNum(userIds.size());
			result.add(teamUserVO);
		}
	}

	private QueryWrapper<Team> getQueryCondition(TeamQuery teamQuery, boolean isAdmin) {
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
		//组合查询条件
		if (teamQuery != null) {
			Long id = teamQuery.getId();
			if (id != null && id > 0) {
				queryWrapper.eq("id", id);
			}
			queryWrapper.in(!CollectionUtils.isEmpty(teamQuery.getTeamIds()),"id", teamQuery.getTeamIds());
			String searchText = teamQuery.getSearchText();
			queryWrapper.and(StringUtils.isNotBlank(searchText), qw -> qw.like("name", searchText).or().like("description", searchText));
			String name = teamQuery.getName();
			queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
			String description = teamQuery.getDescription();
			queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
			Integer maxNum = teamQuery.getMaxNum();
			//查询最大人数相等
			queryWrapper.eq(maxNum != null && maxNum > 0, "maxMum", maxNum);
			Long userId = teamQuery.getUserId();
			//根据创建人来查询
			queryWrapper.eq(userId != null && userId > 0, "userId", userId);
			//根据状态来查询
			Integer status = teamQuery.getStatus();
			TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
			if (statusEnum != null) {
				if (!statusEnum.equals(TeamStatusEnum.PRIVATE)) {
					queryWrapper.eq("status", statusEnum.getValue());
				}
			} else if (!isAdmin) {
				//非管理员默认只展示公开、加密队伍
				queryWrapper.ne("status", TeamStatusEnum.PRIVATE.getValue());
			}

		}
		return queryWrapper;
	}

	/**
	 * 校验要创建的队伍参数
	 *
	 * @param team
	 */
	private void checkTeamInfo(TeamAddRequest team) {
		//1.队伍人数>1且<=20
		int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);//如果为空，直接赋值为0
		if (maxNum < 1 || maxNum > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不满足要求");
		}
		//2.队伍标题 <= 20
		String name = team.getName();
		if (StringUtils.isBlank(name) || name.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题长度不能超过20");
		}
		// 3. 描述 <= 512
		String description = team.getDescription();
		if (StringUtils.isNotBlank(description) && description.length() > 512) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述过长");
		}
		//4.status 是否公开，不传默认为0
		int status = Optional.ofNullable(team.getStatus()).orElse(0);
		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
		if (statusEnum == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
		}

		//5.如果status是加密状态，一定要密码 且密码 <=32
		String password = team.getPassword();
		if (TeamStatusEnum.SECRET.equals(statusEnum)) {
			if (StringUtils.isBlank(password) || password.length() > 32) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
			}
			//加密
			team.setPassword(CommonUtils.encryption(password));
		}
		//6.超出时间 > 当前时间
		Date expireTime = team.getExpireTime();
		if (new Date().after(expireTime)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "超出时间 > 当前时间");
		}

	}

	@Override
	public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
		if (teamUpdateRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Long id = teamUpdateRequest.getId();
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team oldTeam = this.getById(id);
		if (oldTeam == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍不存在");
		}
		//只有管理员或者队伍的创建者才可以修改
		if (!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(oldTeam.getStatus());
		if (TeamStatusEnum.SECRET.equals(statusEnum)) {
			if (StringUtils.isEmpty(teamUpdateRequest.getPassword())) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要有密码");
			}
		}
		Team updateTeam = new Team();
		BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
		return this.updateById(updateTeam);
	}

	@Override
	public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
		if (teamJoinRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Long teamId = teamJoinRequest.getTeamId();
		Long userId = loginUser.getId();
		String password = teamJoinRequest.getPassword();
		checkTeamJoin(teamId, password);
		//修改队伍信息
		return userTeamService.addUserTeam(userId, teamId);
	}

	private void checkTeamJoin(Long teamId, String password) {
		if (teamId == null || teamId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = this.getById(teamId);
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
		}
		Date expireTime = team.getExpireTime();
		if (expireTime != null && expireTime.before(new Date())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
		}
		Integer status = team.getStatus();
		TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
		if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
		}
		if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
			if (StringUtils.isBlank(password) || !CommonUtils.checkPassword(password, team.getPassword())) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
			}
		}
		//已加入队伍的人数
		long teamHasJoinNum = userTeamService.queryJoinTeamUserIds(teamId).size();
		if (teamHasJoinNum >= team.getMaxNum()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
		}

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
		ThrowUtils.throwIf(teamQuitRequest == null || teamQuitRequest.getTeamId() == null || teamQuitRequest.getTeamId() <= 0, ErrorCode.PARAMS_ERROR);
		long userId = loginUser.getId();
		long teamId = teamQuitRequest.getTeamId();
		UserTeam userTeam = userTeamService.query().eq("teamId", teamId).eq("userId", userId).one();
		if (userTeam == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍不存在或未加入队伍");
		}
		long teamHasJoinNum = userTeamService.queryJoinTeamUserIds(teamId).size();
		//队伍只剩下一个人，解散
		if (teamHasJoinNum == 1) {
			//删除队伍
			this.removeById(teamId);
		} else {
			//如果是队长，转移队长
			Team team = getById(teamId);
			ThrowUtils.throwIf(team == null, ErrorCode.SYSTEM_ERROR);
			if (team.getUserId() == userId) {
				//把队伍转移给最早加入的用户
				//1.查询已加入队伍的所有用户和加入时间
				QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
				userTeamQueryWrapper.eq("teamId", teamId);
				userTeamQueryWrapper.last("order by id asc limit 2");
				List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
				if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
					throw new BusinessException(ErrorCode.SYSTEM_ERROR);
				}
				UserTeam nextUserTeam = userTeamList.get(1);
				Long nextTeamLeaderId = nextUserTeam.getUserId();
				//更新当前队伍的队长
				Team updateTeam = new Team();
				updateTeam.setId(teamId);
				updateTeam.setUserId(nextTeamLeaderId);
				boolean result = this.updateById(updateTeam);
				if (!result) {
					throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
				}
			}
		}
		QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("teamId", teamId);
		queryWrapper.eq("userId", userId);
		//移除关系
		return userTeamService.remove(queryWrapper);
	}


	/**
	 * 根据 id 获取队伍信息
	 *
	 * @param teamId
	 * @return
	 */
	private Team getTeamById(Long teamId) {
		if (teamId == null || teamId <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = this.getById(teamId);
		if (team == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "队伍不存在");
		}
		return team;
	}

	@Override
	public List<TeamUserVO> listMyJoinTeams(Long userId, TeamQuery teamQuery) {
		ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR);
		QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userId", userId);
		List<UserTeam> userTeamList = Optional.of(userTeamService.list(queryWrapper)).orElse(new ArrayList<>());
		List<Long> teamIds = userTeamList.stream().map(UserTeam::getTeamId).distinct().collect(Collectors.toList());
		if (teamIds.size() == 0) {
			return new ArrayList<>();
		}
		teamQuery.setTeamIds(teamIds);
		return searchTeam(teamQuery, true);
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteTeam(Long id, User loginUser) {
		// 校验队伍是否存在
		Team team = getTeamById(id);
		long teamId = team.getId();
		// 校验你是不是队伍的队长
		if (!team.getUserId().equals(loginUser.getId())){
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"无访问权限");
		}
		// 移除所有加入队伍的关联信息
		QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
		userTeamQueryWrapper.eq("teamId", teamId);
		boolean result = userTeamService.remove(userTeamQueryWrapper);
		if (!result){
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
		}
		// 删除队伍
		return this.removeById(teamId);
	}
}





