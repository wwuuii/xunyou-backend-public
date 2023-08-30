package com.yuxian.yupao.utils;


import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author yuxian&羽弦
 * date 2023/07/12 17:40
 * description:
 * @version 1.0
 **/
public class RecommendAlgorithmUtils {

	/**
	 * 使用编辑距离算法计算两个字符串集合的相似度
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static int recommendAlgorithmList(List<String> str1, List<String> str2) {
		if (str1 == null || str2 == null) {
			return Integer.MAX_VALUE;
		}
		int str1Len = str1.size();
		int str2Len = str2.size();
		if (str1Len * str2Len == 0) {
			return str1Len + str2Len;
		}
		int[][] d = new int[str1Len + 1][str2Len + 1];
		for (int i = 0; i <= str1Len; i++) {
			d[i][0] = i;
		}
		for (int j = 1; j <= str2Len; j++) {
			d[0][j] = j;
		}
		for (int i = 1; i <= str1Len; i++) {
			String c1 = str1.get(i - 1);
			for (int j = 1; j <= str2Len; j++) {
				String c2 = str2.get(j - 1);
				int tmp = Objects.equals(c1, c2) ? 0 : 1;
				d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + tmp));
			}
		}
		return d[str1Len][str2Len];
	}

	/**
	 * 使用编辑距离算法计算两个字符串的相似度
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static int recommendAlgorithmStr(String str1, String str2) {
		if (StringUtils.isBlank(str1) || StringUtils.isBlank(str2)) {
			return Integer.MAX_VALUE;
		}
		int str1Len = str1.length();
		int str2Len = str2.length();
		if (str1Len * str2Len == 0) {
			return str1Len + str2Len;
		}
		int[][] d = new int[str1Len + 1][str2Len + 1];
		for (int i = 0; i <= str1Len; i++) {
			d[i][0] = i;
		}
		for (int j = 1; j <= str2Len; j++) {
			d[0][j] = j;
		}
		for (int i = 1; i <= str1Len; i++) {
			char c1 = str1.charAt(i - 1);
			for (int j = 1; j <= str2Len; j++) {
				char c2 = str2.charAt(j - 1);
				int tmp = c1 == c2 ? 0 : 1;
				d[i][j] = Math.min(d[i - 1][j] + 1, Math.min(d[i][j - 1] + 1, d[i - 1][j - 1] + tmp));
			}
		}
		return d[str1Len][str2Len];
	}
}
