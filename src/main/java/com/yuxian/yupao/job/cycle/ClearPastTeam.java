package com.yuxian.yupao.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yuxian.yupao.model.entity.Team;
import com.yuxian.yupao.model.entity.UserTeam;
import com.yuxian.yupao.service.TeamService;
import com.yuxian.yupao.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yuxian&羽弦
 * date 2023/07/12 16:51
 * description:
 * @version 1.0
 **/
@Component
@Slf4j
public class ClearPastTeam {

	@Resource
	private UserTeamService userTeamService;
	@Resource
	private TeamService teamService;

	@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
	public void clearPastTeam() {
		List<Long> passTeamIds = Optional.of(teamService.query().le("expireTime", new Date()).list()).orElse(new ArrayList<>()).stream().map(Team::getId).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(passTeamIds)) {
			log.info("暂无过期队伍！");
			return;
		}
		//删除过期队伍
		teamService.removeBatchByIds(passTeamIds);
		QueryWrapper<UserTeam> removeWrapper = new QueryWrapper<>();
		removeWrapper.in("teamId", passTeamIds);
		userTeamService.remove(removeWrapper);
		log.info("成功清除过期队伍！");
	}

}
