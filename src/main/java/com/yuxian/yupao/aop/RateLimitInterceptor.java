package com.yuxian.yupao.aop;

import com.yuxian.yupao.annotation.RedissonRateLimit;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.enums.LimitTypeEnum;
import com.yuxian.yupao.exception.BusinessException;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author yuxian&羽弦
 * date 2023/07/18 11:00
 * description:
 * @version 1.0
 **/
@Aspect
@Slf4j
@Component
public class RateLimitInterceptor {

	@Resource
	private RedissonClient redissonClient;
	@Resource
	private UserService userService;

	@Before("@annotation(redissonRateLimit)")
	public void rateLimit(JoinPoint joinPoint, RedissonRateLimit redissonRateLimit) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		String key = methodSignature.getName();
		if (LimitTypeEnum.USER.getCode().equals(redissonRateLimit.limittype())) {
			Object[] args = joinPoint.getArgs();
			if (args == null || args.length == 0) {
				return;
			}
			HttpServletRequest requests = (HttpServletRequest) args[0];
			User user = userService.getLoginUser(requests);
			key += ":" + user.getId();
		}
		log.info("设置限流器，key={}", key);
		RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
		rateLimiter.trySetRate(redissonRateLimit.rateType(), redissonRateLimit.limit(), redissonRateLimit.timeout(), redissonRateLimit.rateIntervalUnit());
		if (!rateLimiter.tryAcquire(1)) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "请求过于频繁");
		}
	}
}
