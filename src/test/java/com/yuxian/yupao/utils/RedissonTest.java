package com.yuxian.yupao.utils;

import com.yuxian.yupao.annotation.RedissonRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author yuxian&羽弦
 * date 2023/07/10 18:55
 * description:
 * @version 1.0
 **/
@SpringBootTest
@Slf4j
public class RedissonTest {

	@Resource
	private RedissonClient redissonClient;

	@Test
	public void test(){
		RMap<String, String> test = redissonClient.getMap("test");
		test.put("test2", "test2");
		redissonClient.shutdown();
	}

}
