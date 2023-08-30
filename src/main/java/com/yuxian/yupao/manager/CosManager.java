package com.yuxian.yupao.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yuxian.yupao.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;

/**
 * Cos 对象存储操作
 */
@Component
public class CosManager {

	@Resource
	private CosClientConfig cosClientConfig;

	@Resource
	private COSClient cosClient;

	/**
	 * 上传对象
	 *
	 * @param key           唯一键
	 * @param localFilePath 本地文件路径
	 * @return
	 */
	public PutObjectResult putObject(String key, String localFilePath) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
				new File(localFilePath));
		return cosClient.putObject(putObjectRequest);
	}

	/**
	 * 上传对象
	 *
	 * @param key  唯一键 (文件的名称或路径)
	 * @param inputStream 文件
	 * @param contentType 文件类型
	 * @return
	 */
	public PutObjectResult putObject(String key, InputStream inputStream, Long contentLength, String contentType) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(contentLength);
		objectMetadata.setContentType(contentType);
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
				inputStream, objectMetadata);
		return cosClient.putObject(putObjectRequest);
	}
	/**
	 * 上传对象
	 *
	 * @param key  唯一键 (文件的名称或路径)
	 * @param file 文件
	 * @return
	 */
	public PutObjectResult putObject(String key, File file) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
				file);
		return cosClient.putObject(putObjectRequest);
	}

}
