package com.yuxian.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuxian.yupao.model.entity.UserTeam;

import java.util.List;

/**
* @author admin
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2023-07-09 16:56:04
*/
public interface UserTeamService extends IService<UserTeam> {

	/**
	 * 添加 队伍关系 到关系表
	 * @param userId
	 * @param teamId
	 * @return
	 */
	boolean addUserTeam(Long userId, Long teamId);


	/**
	 * 查询某个队伍已加入的用户Id
	 * @param teamId
	 * @return
	 */
	List<Long> queryJoinTeamUserIds(Long teamId);

}
