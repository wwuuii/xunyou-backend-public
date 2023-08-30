package com.yuxian.yupao.controller;

import com.yuxian.yupao.common.BaseResponse;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.common.ResultUtils;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.model.dto.privateMessage.MessageListRespDto;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.service.UserMessageService;
import com.yuxian.yupao.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * @author yuxian&羽弦
 * date 2023/07/13 23:39
 * description:
 * @version 1.0
 **/
@RestController
@RequestMapping("/userMessage")
public class UserMessageController {

	@Resource
	private UserMessageService userMessageService;
	@Resource
	private UserService userService;

	@GetMapping("/getMessageList")
	public BaseResponse<List<MessageListRespDto>> getMessageList(@RequestParam Long receiverId, HttpServletRequest servletRequest) {
		User user = userService.getLoginUser(servletRequest);
		ThrowUtils.throwIf(!Objects.equals(user.getId(), receiverId), ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(userMessageService.getMessageList(receiverId));
	}
}
