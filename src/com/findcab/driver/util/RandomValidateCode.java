package com.findcab.driver.util;

import java.util.Random;

public class RandomValidateCode {

	private static Random random = new Random();
	private static String randString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";// 随机产生的字符串

	private static int stringNum = 4;// 随机产生字符数量

	/*
	 * 获取随机的字符串
	 */
	public static String getRandomString() {
		String rand = "";
		for (int i = 0; i < stringNum; i++) {

			rand += String.valueOf(getRandomChar(random.nextInt(randString
					.length())));
		}

		return rand;
	}

	/*
	 * 获取随机的字符
	 */
	private static String getRandomChar(int num) {
		return String.valueOf(randString.charAt(num));
	}
}