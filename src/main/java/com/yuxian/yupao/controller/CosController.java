package com.yuxian.yupao.controller;

import com.yuxian.yupao.annotation.RedissonRateLimit;
import com.yuxian.yupao.common.BaseResponse;
import com.yuxian.yupao.common.ResultUtils;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.service.CosService;
import com.yuxian.yupao.service.UserService;
import org.redisson.api.RateIntervalUnit;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author yuxian&羽弦
 * date 2023/07/16 14:08
 * description:
 * @version 1.0
 **/
@RestController
@RequestMapping("/cos")
public class CosController {

	@Resource
	private CosService cosService;
	@Resource
	private UserService userService;

	@PostMapping("/uploadAvatar")
	@RedissonRateLimit(limit = 1, limittype = 1, rateIntervalUnit = RateIntervalUnit.MINUTES)
	public BaseResponse<String> uploadAvatar(HttpServletRequest servletRequest, @RequestPart("multipartFile") MultipartFile multipartFile) {
		User loginUser = userService.getLoginUser(servletRequest);
		return ResultUtils.success(cosService.uploadAvatar(multipartFile, loginUser.getId()));
	}
}
