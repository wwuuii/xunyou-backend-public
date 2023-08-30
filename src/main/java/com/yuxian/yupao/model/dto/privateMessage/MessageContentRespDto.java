package com.yuxian.yupao.model.dto.privateMessage;

import lombok.Data;

import java.util.Date;

@Data
public class MessageContentRespDto {

	private Long id;
	private Boolean isSender;
	private String messageContent;
	private Date createTime;
}