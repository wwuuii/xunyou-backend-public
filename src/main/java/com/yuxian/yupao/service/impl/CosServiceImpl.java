package com.yuxian.yupao.service.impl;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.config.CosClientConfig;
import com.yuxian.yupao.constant.ContentTypeConstant;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.manager.CosManager;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.service.CosService;
import com.yuxian.yupao.service.UserService;
import com.yuxian.yupao.utils.CommonUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;

/**
 * @author yuxian&羽弦
 * date 2023/07/16 14:12
 * description:
 * @version 1.0
 **/
@Service
public class CosServiceImpl implements CosService {

	@Resource
	private CosManager cosManager;
	@Resource
	private UserService userService;
	@Resource
	private CosClientConfig cosClientConfig;

	@Override
	public String uploadAvatar(MultipartFile multipartFile, Long userId) {
		ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
		checkSize(multipartFile, 1000000L);
		String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
		ThrowUtils.throwIf(!"jpg".equals(suffix), ErrorCode.PARAMS_ERROR, "图片格式必须是jpeg格式");
		String filePath = CommonUtils.getFilePath("/userAvatar/", multipartFile.getOriginalFilename());
		try {
			cosManager.putObject(filePath, multipartFile.getInputStream(), multipartFile.getSize(), ContentTypeConstant.JPEG);
			User user = new User();
			user.setId(userId);
			String result = cosClientConfig.getFileUrl() + filePath;
			user.setUserAvatar(result);
			userService.updateById(user);
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param multipartFile
	 * @param maxSize       单位 byte
	 */
	private void checkSize(MultipartFile multipartFile, Long maxSize) {
		ThrowUtils.throwIf(maxSize == null || maxSize <= 0, ErrorCode.SYSTEM_ERROR);
		long size = multipartFile.getSize();
		ThrowUtils.throwIf(size > maxSize, ErrorCode.PARAMS_ERROR, String.format("上传文件的大小不能超过%sM", maxSize / 1000000));
	}

}
