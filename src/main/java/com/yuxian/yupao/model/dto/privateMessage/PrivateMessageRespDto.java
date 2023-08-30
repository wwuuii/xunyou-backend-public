package com.yuxian.yupao.model.dto.privateMessage;

import lombok.Data;

import java.util.List;

/**
 * @author yuxian&羽弦
 * date 2023/07/14 11:38
 * description:
 * @version 1.0
 **/
@Data
public class PrivateMessageRespDto {

	private Long senderId;
	private String senderAvatar;
	private String receiverAvatar;
	private String receiverName;
	private List<MessageContentRespDto> messageContentRespDtos;

}
