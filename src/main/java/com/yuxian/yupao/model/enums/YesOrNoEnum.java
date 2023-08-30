package com.yuxian.yupao.model.enums;

import lombok.AllArgsConstructor;

/**
 * @author yuxian&羽弦
 * date 2023/07/05 12:32
 * description:
 * @version 1.0
 **/
@AllArgsConstructor
public enum YesOrNoEnum {

	YES("是", 1),
	NO("否",0),;

	private String name;
	private Integer code;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
}
