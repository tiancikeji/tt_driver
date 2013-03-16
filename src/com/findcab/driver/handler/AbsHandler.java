package com.findcab.driver.handler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

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
			String responseStr = streamToString(inputStream);
			reponseResult = parseResponse(responseStr);
		} catch (Exception e) {
			Log.i("AbsHandler", e.getMessage());
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
}
