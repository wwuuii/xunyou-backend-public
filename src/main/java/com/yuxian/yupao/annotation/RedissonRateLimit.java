package com.yuxian.yupao.annotation;

import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yuxian&羽弦
 * date 2023/07/18 10:58
 * description:
 * @version 1.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonRateLimit {

	int limit() default 10; // 设置默认的限流阈值
	int timeout() default 1; // 设置默认的超时时间
	int limittype() default 0; // 0：根据方法维度限流，1：根据用户维度限流
	RateType rateType() default RateType.OVERALL;
	RateIntervalUnit rateIntervalUnit() default RateIntervalUnit.SECONDS;
}
