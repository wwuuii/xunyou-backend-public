package com.yuxian.yupao.enums;

/**
 * @author yuxian&羽弦
 * date 2023/07/13 17:36
 * description:
 * @version 1.0
 **/
public enum UserStatusEnum {
	VISIBLE(0, "可见"),
	UN_VISIBLE(1, "不可见");

	int code;
	String name;

	UserStatusEnum(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
