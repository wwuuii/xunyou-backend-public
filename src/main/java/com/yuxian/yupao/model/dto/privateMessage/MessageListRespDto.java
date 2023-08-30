package com.yuxian.yupao.model.dto.privateMessage;

import lombok.Data;

import java.util.Date;

/**
 * @author yuxian&羽弦
 * date 2023/07/13 22:33
 * description:
 * @version 1.0
 **/
@Data
public class MessageListRespDto {

	private Long senderId;
	private String messageContent;
	private String userAvatar;
	private String userName;
	private Long unReadNum;
	private Date updateTime;
}
