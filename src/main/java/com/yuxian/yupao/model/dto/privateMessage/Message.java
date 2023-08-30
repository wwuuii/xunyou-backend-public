package com.yuxian.yupao.model.dto.privateMessage;

import lombok.Data;

/**
 * @author yuxian&羽弦
 * date 2023/07/14 17:31
 * description:
 * @version 1.0
 **/
@Data
public class Message {

	private Long id;
	private String content;
	private String senderId;
	private String receiverId;
}
