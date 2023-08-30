package com.yuxian.yupao.enums;

/**
 * @author yuxian&羽弦
 * date 2023/07/18 12:33
 * description:
 * @version 1.0
 **/
public enum LimitTypeEnum {

	METHOD(0),
	USER(1);
	private Integer code;

	LimitTypeEnum(Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
}
