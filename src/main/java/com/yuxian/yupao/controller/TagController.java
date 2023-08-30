package com.yuxian.yupao.controller;

import com.yuxian.yupao.common.BaseResponse;
import com.yuxian.yupao.common.ResultUtils;
import com.yuxian.yupao.model.dto.tag.TagRespDto;
import com.yuxian.yupao.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author yuxian&羽弦
 * date 2023/07/15 22:45
 * description:
 * @version 1.0
 **/
@RestController
@RequestMapping("/tag")
public class TagController {

	@Resource
	private TagService tagService;

	@GetMapping("/list")
	public BaseResponse<List<TagRespDto>> queryTags() {
		return ResultUtils.success(tagService.queryTags());
	}

	@PostMapping("/getTagIds")
	public BaseResponse<List<Long>> getTagIds(@RequestBody Set<String> tagNames) {
		return ResultUtils.success(tagService.getTagIds(tagNames));
	}
}
