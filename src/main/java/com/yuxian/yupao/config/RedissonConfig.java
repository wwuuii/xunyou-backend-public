package com.yuxian.yupao.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuxian&羽弦
 * date 2023/07/10 18:03
 * description:
 * @version 1.0
 **/
@Configuration
public class RedissonConfig {

	@Value("${spring.redis.host}")
	private String host;
	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
				.setAddress("redis://"+host+":6379")
				.setDatabase(1);

		return Redisson.create(config);
	}
}
