package com.yuxian.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuxian.yupao.model.dto.privateMessage.MessageListRespDto;
import com.yuxian.yupao.model.entity.UserMessage;

import java.util.List;

/**
* @author admin
* @description 针对表【user_message(用户消息表)】的数据库操作Service
* @createDate 2023-07-13 23:05:55
*/
public interface UserMessageService extends IService<UserMessage> {

	/**
	 * 获取receiverId的消息
	 *
	 * @param receiverId
	 * @return
	 */
	List<MessageListRespDto> getMessageList(Long receiverId);

	boolean saveOrUpdate(Long receiverId, Long senderId, String messageContent);
}
