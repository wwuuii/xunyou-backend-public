package com.yuxian.yupao.mapper;

import com.qcloud.cos.model.PutObjectResult;
import com.yuxian.yupao.constant.ContentTypeConstant;
import com.yuxian.yupao.manager.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author yuxian&羽弦
 * date 2023/07/16 21:12
 * description:
 * @version 1.0
 **/
@SpringBootTest
public class CosManagerTest {

	@Resource
	private CosManager cosManager;

	@Test
	public void test() {
		String filePath = "D:\\3.jpg";
		File file = new File(filePath);
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			PutObjectResult putObjectResult = cosManager.putObject("3.jpg", fileInputStream, file.length(), ContentTypeConstant.JPEG);
			System.out.println(putObjectResult);
		}catch (Exception e) {

		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (Exception e) {

			}

		}

	}
}
