package com.yuxian.yupao.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.mapper.UserMessageMapper;
import com.yuxian.yupao.model.dto.privateMessage.MessageListRespDto;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.entity.UserMessage;
import com.yuxian.yupao.service.PrivateMessageService;
import com.yuxian.yupao.service.UserMessageService;
import com.yuxian.yupao.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author admin
* @description 针对表【user_message(用户消息表)】的数据库操作Service实现
* @createDate 2023-07-13 23:05:55
*/
@Service
public class UserMessageServiceImpl extends ServiceImpl<UserMessageMapper, UserMessage>
    implements UserMessageService {

	@Resource
	private UserService userService;
	@Resource
	private PrivateMessageService privateMessageService;

	/**
	 * 获取receiverId的消息
	 */
	@Override
	public List<MessageListRespDto> getMessageList(Long receiverId) {
		ThrowUtils.throwIf(receiverId == null || receiverId <= 0, ErrorCode.PARAMS_ERROR);
		QueryWrapper<UserMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("receiverId", receiverId);
		//todo 数据量大时做分页
		List<UserMessage> userMessages = list(queryWrapper);
		return userMessagesToMessageListResp(userMessages, receiverId);
	}

	@Override
	public boolean saveOrUpdate(Long receiverId, Long senderId, String messageContent) {
		UserMessage userMessage = new UserMessage();
		userMessage.setMessageContent(messageContent);
		userMessage.setSenderId(senderId);
		userMessage.setReceiverId(receiverId);
		QueryWrapper<UserMessage> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("senderId", senderId);
		queryWrapper.eq("receiverId", receiverId);
		return saveOrUpdate(userMessage, queryWrapper);
	}

	private List<MessageListRespDto> userMessagesToMessageListResp(List<UserMessage> userMessages, Long receiverId) {
		if (CollectionUtils.isEmpty(userMessages)) {
			return new ArrayList<>();
		}
		List<Long> senderIds = userMessages.stream().map(UserMessage::getSenderId).collect(Collectors.toList());
		//查询用户信息
		Map<Long, User> userMaps = userService.batchQueryByIds(senderIds).stream().collect(Collectors.toMap(User::getId, user -> user));
		List<MessageListRespDto> result = new ArrayList<>();
		userMessages.forEach(userMessage -> {
			MessageListRespDto messageListRespDto = new MessageListRespDto();
			BeanUtils.copyProperties(userMessage, messageListRespDto);
			User user = userMaps.getOrDefault(userMessage.getSenderId(), null);
			if (user != null) {
				messageListRespDto.setUserAvatar(user.getUserAvatar());
				messageListRespDto.setUserName(user.getUserName());
			}
			//设置消息未读数量
			messageListRespDto.setUnReadNum(privateMessageService.getUnReadNum(receiverId, userMessage.getSenderId()));
			result.add(messageListRespDto);
		});
		return result;
	}
}




