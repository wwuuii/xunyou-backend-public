package com.yuxian.yupao.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author yuxian&羽弦
 * date 2023/07/16 14:11
 * description:
 * @version 1.0
 **/
public interface CosService {


	/**
	 * 上传头像
	 *
	 * @param multipartFile
	 * @return
	 */
	String uploadAvatar(MultipartFile multipartFile, Long userId);
}
