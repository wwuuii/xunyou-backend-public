package com.yuxian.yupao.job.cycle;

import com.yuxian.yupao.constant.RedisConstant;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.vo.user.UserVO;
import com.yuxian.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author yuxian&羽弦
 * date 2023/07/09 10:18
 * description:
 * @version 1.0
 **/
@Component
@Slf4j
public class PreRecommendJob {

	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private UserService userService;

	@PostConstruct
	public void init() {
		work();
	}
	@Scheduled(cron = "0 0 4 * * ? ")
	public void preRecommendToCache() {
		work();
	}

	private void work() {
		//查出近两天登陆的用户
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime twoDaysAgo = now.minusDays(2);
		List<User> latestLogin = userService.query().ge("latestLogin", twoDaysAgo).list();

		//缓存用户主页推荐信息
//		latestLogin.forEach(user -> {
//			List<UserVO> recommend = userService.recommend(user, 1);
//			redisTemplate.opsForValue().set(RedisConstant.RECOMMEND_KEY + user.getId(), recommend, 1, TimeUnit.DAYS);
//		});
//		log.info("end timer: preRecommendToCache");
		log.info("start timer: preMatchToCache");
		latestLogin.forEach(user -> {
			List<UserVO> userVOS = userService.matchUsers(1, user);
			redisTemplate.opsForValue().set(RedisConstant.MATCH_KEY + user.getId(), userVOS, 1, TimeUnit.DAYS);
		});
		log.info("end timer: preMatchToCache");
	}


}
