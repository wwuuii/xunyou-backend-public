package com.yuxian.yupao.service;

import com.yuxian.yupao.model.dto.tag.TagRespDto;
import com.yuxian.yupao.model.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import springfox.documentation.service.Tags;

import java.util.List;
import java.util.Set;

/**
* @author admin
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2023-07-05 10:44:09
*/
public interface TagService extends IService<Tag> {

	/**
	 * 添加标签
	 *
	 * @param tag
	 * @return
	 */
	boolean addTag(Tag tag);

	List<TagRespDto> queryTags();

	List<Tag> queryByIds(Set<Long> ids);

	/**
	 * 根据标签名获取标签Id
	 *
	 * @param tagNames
	 * @return
	 */
	List<Long> getTagIds(Set<String> tagNames);
}
