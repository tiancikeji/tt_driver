package com.findcab.driver.handler;

import java.io.InputStream; 
/**
 * 
 * @author zhangkun
 * @date 2011-12-1
 */
public interface Ihandler {
	public Object parseResponse(InputStream inputStream) throws Exception;

//	void onBookLoad(BookInfo bookInfo);
}
