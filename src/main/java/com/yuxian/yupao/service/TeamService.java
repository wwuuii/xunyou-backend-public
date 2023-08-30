package com.yuxian.yupao.service;

import com.yuxian.yupao.model.dto.team.*;
import com.yuxian.yupao.model.entity.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.vo.team.TeamUserVO;

import java.util.List;

/**
* @author admin
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-07-09 16:53:54
*/
public interface TeamService extends IService<Team> {
	/**
	 * 创建队伍
	 *
	 * @param team
	 * @param loginUser
	 * @return
	 */
	long addTeam(TeamAddRequest team, User loginUser);

	/**
	 * 搜索队伍
	 * @param teamQuery
	 * @return
	 */
	List<TeamUserVO> searchTeam(TeamQuery teamQuery, boolean isAdmin);

	/**
	 * 修改队伍
	 *
	 * @param teamUpdateRequest
	 * @param loginUser
	 * @return
	 */
	boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

	/**
	 * 加入队伍
	 *
	 * @param teamJoinRequest
	 * @param loginUser
	 * @return
	 */
	boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

	/**
	 * 退出队伍
	 * @param teamQuitRequest
	 * @param loginUser
	 * @return
	 */
	boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

	/**
	 * 删除队伍
	 * @param id
	 * @param loginUser
	 * @return
	 */
	boolean deleteTeam(Long id, User loginUser);


	/**
	 * 查询用户Id加入的队伍
	 * @param userId
	 * @param teamQuery
	 * @return
	 */
	List<TeamUserVO> listMyJoinTeams(Long userId, TeamQuery teamQuery);
}
