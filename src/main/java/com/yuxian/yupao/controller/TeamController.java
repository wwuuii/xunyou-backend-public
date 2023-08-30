package com.yuxian.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuxian.yupao.common.BaseResponse;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.common.ResultUtils;
import com.yuxian.yupao.exception.BusinessException;
import com.yuxian.yupao.model.dto.team.*;
import com.yuxian.yupao.model.entity.Team;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.entity.UserTeam;
import com.yuxian.yupao.model.vo.team.TeamUserVO;
import com.yuxian.yupao.service.TeamService;
import com.yuxian.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author yuxian&羽弦
 * date 2023/07/09 16:58
 * description:
 * @version 1.0
 **/
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {

	@Resource
	private TeamService teamService;
	@Resource
	private UserService userService;

	@PostMapping("/add")
	public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest team, HttpServletRequest request) {
		User user = userService.getLoginUser(request);
		if (team == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		return ResultUtils.success(teamService.addTeam(team, user));
	}

	@DeleteMapping("/delete")
	public BaseResponse<Boolean> deleteTeam(@RequestParam Long id,HttpServletRequest request) {
		if (id == null || id <= 0L) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.deleteTeam(id,loginUser);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
		}
		return ResultUtils.success(true);
	}

	@PostMapping("/update")
	public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
		if (!result) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
		}
		return ResultUtils.success(true);
	}

	@GetMapping("/get")
	public BaseResponse<Team> getTeamById(@RequestParam("id") long id) {
		if (id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = teamService.getById(id);
		if (team == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		return ResultUtils.success(team);
	}

	/**
	 * 获取我创建的队伍
	 *
	 * @param teamQuery
	 * @param request
	 * @return
	 */
	@GetMapping("/list/my/create")
	public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);
		teamQuery.setUserId(loginUser.getId());
		List<TeamUserVO> teamList = teamService.searchTeam(teamQuery, true);
		return ResultUtils.success(teamList);
	}

	/**
	 * 获取我加入的队伍
	 *
	 * @param teamQuery
	 * @param request
	 * @return
	 */
	@GetMapping("/list/my/join")
	public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		User loginUser = userService.getLoginUser(request);

		return ResultUtils.success(teamService.listMyJoinTeams(loginUser.getId(), teamQuery));
	}

	@GetMapping("/list")
	public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		userService.getLoginUser(request);
		boolean isAdmin = userService.isAdmin(request);
		List<TeamUserVO> teamList = teamService.searchTeam(teamQuery, isAdmin);
		return ResultUtils.success(teamList);
	}

	@GetMapping("/list/page")
	public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
		if (teamQuery == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		Team team = new Team();
		BeanUtils.copyProperties(team, teamQuery);
		Page<Team> page = new Page<>(teamQuery.getCurrent(), teamQuery.getPageSize());
		QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
		Page<Team> resultPage = teamService.page(page, queryWrapper);
		return ResultUtils.success(resultPage);
	}

	@PostMapping("/join")
	public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
		return ResultUtils.success(result);
	}

	@PostMapping("/quit")
	public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
		User loginUser = userService.getLoginUser(request);
		boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
		return ResultUtils.success(result);
	}


}
