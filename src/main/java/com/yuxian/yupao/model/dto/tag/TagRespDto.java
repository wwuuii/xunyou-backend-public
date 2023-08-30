package com.yuxian.yupao.model.dto.tag;

import com.yuxian.yupao.model.entity.Tag;
import lombok.Data;

import java.util.List;

/**
 * @author yuxian&羽弦
 * date 2023/07/15 22:34
 * description:
 * @version 1.0
 **/
@Data
public class TagRespDto {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 标签名
	 */
	private String text;


	private List<TagRespDto> children;


	private static final long serialVersionUID = 1L;
}
