package com.yuxian.yupao.controller;

import com.yuxian.yupao.common.BaseResponse;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.common.ResultUtils;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.model.dto.privateMessage.PrivateMessageRespDto;
import com.yuxian.yupao.model.dto.privateMessage.SendMessageReqDto;
import com.yuxian.yupao.model.dto.privateMessage.UnreadNumRespDto;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.service.PrivateMessageService;
import com.yuxian.yupao.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author yuxian&羽弦
 * date 2023/07/13 22:31
 * description:
 * @version 1.0
 **/
@RestController
@RequestMapping("/privateMessage")
public class PrivateMessageController {

	@Resource
	private PrivateMessageService privateMessageService;
	@Resource
	private UserService userService;

	@GetMapping("/{receiverId}")
	public BaseResponse<PrivateMessageRespDto> getPrivateMessage(@PathVariable("receiverId") Long receiverId, HttpServletRequest httpServletRequest) {
		User loginUser = userService.getLoginUser(httpServletRequest);
		return ResultUtils.success(privateMessageService.getPrivateMessage(loginUser, receiverId));
	}

	@GetMapping("/getUnreadNumAll")
	public BaseResponse<UnreadNumRespDto> getUnreadNumAll(HttpServletRequest httpServletRequest) {
		User loginUser = userService.getLoginUser(httpServletRequest);
		Long unReadNum = privateMessageService.getUnReadNumAll(loginUser.getId());
		UnreadNumRespDto unreadNumRespDto = new UnreadNumRespDto();
		unreadNumRespDto.setNum(unReadNum);
		return ResultUtils.success(unreadNumRespDto);
	}

	@PostMapping("/sendMessage")
	public BaseResponse<Boolean> sendMessage(@RequestBody SendMessageReqDto sendMessageReqDto, HttpServletRequest httpServletRequest) {
		User loginUser = userService.getLoginUser(httpServletRequest);
		privateMessageService.sendMessage(loginUser, sendMessageReqDto.getReceiverId(), sendMessageReqDto.getMessage());
		return ResultUtils.success(true);
	}

	@PostMapping("batchUpdateStatus")
	public BaseResponse<Boolean> batchUpdateStatus( HttpServletRequest httpServletRequest) {
		User loginUser = userService.getLoginUser(httpServletRequest);
		privateMessageService.batchUpdateUnRead(loginUser.getId());
		return ResultUtils.success(Boolean.TRUE);
	}
}
