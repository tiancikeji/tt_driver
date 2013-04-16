package com.findcab.driver.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 抽象处理器
 * 
 * @author zhangkun
 * @date 2011-12-14
 */
public abstract class AbsHandler implements Ihandler {

	@Override
	public Object parseResponse(InputStream inputStream) throws Exception {
		Object reponseResult = null;
		try {
			// String responseStr = streamToString(inputStream);

			String responseStr = inputStreamToString(inputStream,"UTF-8");
			responseStr = new String(responseStr.getBytes("UTF-8"));

			reponseResult = parseResponse(responseStr);
		} catch (Exception e) {
//			Log.i("AbsHandler", e.getMessage());
			throw e;
		}
		return reponseResult;
	}

	abstract public Object parseResponse(String responseStr) throws Exception;

	private String streamToString(InputStream inputStream) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		StringBuffer sb = new StringBuffer();
		String str = null;
		while ((str = bufferedReader.readLine()) != null) {
			sb.append(str);

		}
		return sb.toString();
	}

	private String Stream2String(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				16 * 1024); // 强制缓存大小为16KB，一般Java类默认为8KB
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) { // 处理换行符
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private String inputStreamToString(InputStream is, String encoding) {
		try {
			byte[] b = new byte[1024];
			String res = "";
			if (is == null) {
				return "";
			}

			int bytesRead = 0;
			while (true) {
				bytesRead = is.read(b, 0, 1024); // return final read bytes
													// counts
				if (bytesRead == -1) {// end of InputStream
					return res;
				}
				res += new String(b, 0, bytesRead, encoding); // convert to
																// string using
																// bytes
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.print("Exception: " + e);
			return "";
		}
	}

}
