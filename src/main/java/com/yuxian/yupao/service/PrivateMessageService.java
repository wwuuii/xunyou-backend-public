package com.yuxian.yupao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yuxian.yupao.model.dto.privateMessage.PrivateMessageRespDto;
import com.yuxian.yupao.model.entity.PrivateMessage;
import com.yuxian.yupao.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author admin
 * @description 针对表【private_message(私聊消息表)】的数据库操作Service
 * @createDate 2023-07-13 22:24:24
 */
public interface PrivateMessageService extends IService<PrivateMessage> {


	/**
	 * 获取私聊信息
	 * @param loginUser
	 * @param receiverId
	 * @return
	 */
	PrivateMessageRespDto getPrivateMessage(User loginUser, Long receiverId);

	/**
	 * 发送私聊消息
	 * @param loginUser
	 * @param receiverId
	 * @param message
	 */
	void sendMessage(User loginUser, Long receiverId, String message);

	/**
	 * 获取某个对话的未读消息数
	 *
	 * @param receiverId
	 * @param senderId
	 * @return
	 */
	Long getUnReadNum(Long receiverId, Long senderId);
	/**
	 * 获取所有未读消息数
	 *
	 * @param receiverId
	 * @return
	 */
	Long getUnReadNumAll(Long receiverId);

	/**
	 * 批量设置消息已读
	 * @param receiverId
	 */
	void batchUpdateUnRead(Long receiverId);
}
