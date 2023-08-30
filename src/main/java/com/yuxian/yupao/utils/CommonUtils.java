package com.yuxian.yupao.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.BCrypt;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author yuxian&羽弦
 * date 2023/07/12 10:54
 * description:
 * @version 1.0
 **/
public class CommonUtils {

	/**
	 * 字符串加密
	 *
	 * @param str
	 * @return
	 */
	public static String encryption(String str) {
		String salt = BCrypt.gensalt();
		return BCrypt.hashpw(str, salt);
	}

	/**
	 * 校验密码
	 * @param password 未加密
	 * @param hashPassword 已加密
	 * @return
	 */
	public static boolean checkPassword(String password, String hashPassword) {
		return BCrypt.checkpw(password, hashPassword);
	}

	/**
	 * 获取上传COS的文件路径
	 * @param fileName
	 * @return
	 */
	public static String getFilePath(String prefix, String fileName) {
		String suffix = FileUtil.getSuffix(fileName);
		// 获取当前日期
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("/yyyy/MM/dd/");
		// 格式化日期
		String formattedDate = currentDate.format(formatter);
		return formattedDate + prefix + IdUtil.randomUUID() + suffix;
	}
}
