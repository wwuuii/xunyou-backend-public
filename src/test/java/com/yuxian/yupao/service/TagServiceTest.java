package com.yuxian.yupao.service;

import com.yuxian.yupao.model.entity.Tag;
import com.yuxian.yupao.model.enums.YesOrNoEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author yuxian&羽弦
 * date 2023/07/05 12:28
 * description:
 * @version 1.0
 **/
@SpringBootTest
public class TagServiceTest {

	@Resource
	private TagService tagService;

	@Test
	void addTest() {

		Tag tag = new Tag();
		tag.setTagName("Python");
		tag.setIsParent(YesOrNoEnum.NO.getCode());
		tag.setParentId(1676449988153864194L);
		tagService.addTag(tag);
	}
}
