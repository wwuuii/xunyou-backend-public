package com.yuxian.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.mapper.PrivateMessageMapper;
import com.yuxian.yupao.model.dto.privateMessage.Message;
import com.yuxian.yupao.model.dto.privateMessage.MessageContentRespDto;
import com.yuxian.yupao.model.dto.privateMessage.PrivateMessageRespDto;
import com.yuxian.yupao.model.entity.PrivateMessage;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.entity.UserMessage;
import com.yuxian.yupao.service.PrivateMessageService;
import com.yuxian.yupao.service.UserMessageService;
import com.yuxian.yupao.service.UserService;
import com.yuxian.yupao.ws.GlobalWS;
import com.yuxian.yupao.ws.PrivateMessageWS;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author admin
 * @description 针对表【private_message(私聊消息表)】的数据库操作Service实现
 * @createDate 2023-07-13 22:24:24
 */
@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
		implements PrivateMessageService {

	@Resource
	private UserService userService;
	@Resource
	private UserMessageService userMessageService;

	@Override
	public PrivateMessageRespDto getPrivateMessage(User loginUser, Long receiverId) {
		ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR);
		PrivateMessageRespDto result = new PrivateMessageRespDto();
		result.setSenderAvatar(loginUser.getUserAvatar());
		result.setSenderId(loginUser.getId());
		//查询接收者的头像与用户Id
		User receiverUser = userService.getById(receiverId);
		result.setReceiverAvatar(receiverUser.getUserAvatar());
		result.setReceiverName(receiverUser.getUserName());
		List<MessageContentRespDto> messageContentRespDtos = new ArrayList<>();
		//我发送给别人的消息
		getPrivateMessage(loginUser.getId(), receiverId).forEach(msg -> {
			MessageContentRespDto messageContentRespDto = new MessageContentRespDto();
			messageContentRespDto.setIsSender(true);
			messageContentRespDto.setId(msg.getId());
			messageContentRespDto.setMessageContent(msg.getMessageContent());
			messageContentRespDto.setCreateTime(msg.getCreateTime());
			messageContentRespDtos.add(messageContentRespDto);
		});
		//别人发送给我的消息
		getPrivateMessage(receiverId, loginUser.getId()).forEach(msg -> {
			MessageContentRespDto messageContentRespDto = new MessageContentRespDto();
			messageContentRespDto.setIsSender(false);
			messageContentRespDto.setId(msg.getId());
			messageContentRespDto.setMessageContent(msg.getMessageContent());
			messageContentRespDto.setCreateTime(msg.getCreateTime());
			messageContentRespDtos.add(messageContentRespDto);
		});
		//按时间升序
		messageContentRespDtos.sort(Comparator.comparing(MessageContentRespDto::getCreateTime));
		result.setMessageContentRespDtos(messageContentRespDtos);
		return result;
	}

	@Override
	@Transactional
	public void sendMessage(User loginUser, Long receiverId, String message) {
		ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "要发送的消息为NULL");
		PrivateMessage privateMessage = new PrivateMessage();
		privateMessage.setStatus(0);
		privateMessage.setReceiverId(receiverId);
		privateMessage.setMessageContent(message);
		privateMessage.setSenderId(loginUser.getId());
		boolean saveResult = save(privateMessage);
		ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR);
		//向消息接收者发送消息
		Message messageDto = new Message();
		messageDto.setId(privateMessage.getId());
		messageDto.setContent(message);
		messageDto.setSenderId(String.valueOf(loginUser.getId()));
		messageDto.setReceiverId(String.valueOf(receiverId));
		PrivateMessageWS.sendOneMessage(receiverId, messageDto);
		GlobalWS.sendOneMessage(receiverId, messageDto);
		//存储userMessage表
		userMessageService.saveOrUpdate(receiverId, loginUser.getId(), message);
		userMessageService.saveOrUpdate(loginUser.getId(), receiverId, message);
	}

	@Override
	public Long getUnReadNum(Long receiverId, Long senderId) {
		ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR);
		QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("receiverId", receiverId);
		queryWrapper.eq("senderId", senderId);
		queryWrapper.eq("status", 0);
		return count(queryWrapper);
	}
	@Override
	public Long getUnReadNumAll(Long receiverId) {
		ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR);
		QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("receiverId", receiverId);
		queryWrapper.eq("status", 0);
		return count(queryWrapper);
	}
	@Override
	public void batchUpdateUnRead(Long receiverId) {
		ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR);
		update().eq("receiverId", receiverId).set("status", 1).update();
	}

	private List<PrivateMessage> getPrivateMessage(Long senderId, Long receiverId) {
		QueryWrapper<PrivateMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id", "messageContent", "createTime");
		queryWrapper.eq("senderId", senderId);
		queryWrapper.eq("receiverId", receiverId);
		return list(queryWrapper);
	}
}




