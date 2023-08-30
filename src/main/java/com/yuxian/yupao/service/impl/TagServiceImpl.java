package com.yuxian.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuxian.yupao.model.dto.tag.TagRespDto;
import com.yuxian.yupao.model.entity.Tag;
import com.yuxian.yupao.service.TagService;
import com.yuxian.yupao.mapper.TagMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author admin
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2023-07-05 10:44:09
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

	@Resource
	private TagMapper tagMapper;

	@Override
	public boolean addTag(Tag tag) {
		return tagMapper.insert(tag) > 0;
	}

	@Override
	public List<TagRespDto> queryTags() {
		List<Tag> parentTags = query().eq("isParent", 1).list();
		if (CollectionUtils.isEmpty(parentTags)) {
			return new ArrayList<>();
		}
		return parentTags.stream().map(this::setTagChild).collect(Collectors.toList());
	}

	@Override
	public List<Tag> queryByIds(Set<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}
		return query().in("id", ids).list();
	}

	@Override
	public List<Long> getTagIds(Set<String> tagNames) {
		if (CollectionUtils.isEmpty(tagNames)) {
			return new ArrayList<>();
		}
		QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
		queryWrapper.in("tagName", tagNames);
		return Optional.ofNullable(list(queryWrapper)).orElse(new ArrayList<>()).stream().map(Tag::getId).collect(Collectors.toList());
	}

	private TagRespDto setTagChild(Tag parentTag) {
		if (parentTag == null) {
			return new TagRespDto();
		}
		List<Tag> childTags = query().eq("parentId", parentTag.getId()).list();
		TagRespDto result = tagToTagRespDto(parentTag);
		List<TagRespDto> childTagRespDtos = childTags.stream().map(this::tagToTagRespDto).collect(Collectors.toList());
		result.setChildren(childTagRespDtos);
		return result;
	}

	private TagRespDto tagToTagRespDto(Tag tag) {
		TagRespDto result = new TagRespDto();
		result.setText(tag.getTagName());
		result.setId(tag.getId());
		return result;
	}
}




