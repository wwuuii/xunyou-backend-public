package com.yuxian.yupao.model.dto.privateMessage;

import lombok.Data;

/**
 * @author yuxian&羽弦
 * date 2023/07/14 16:41
 * description:
 * @version 1.0
 **/
@Data
public class SendMessageReqDto {

	private String message;
	private Long receiverId;
}
