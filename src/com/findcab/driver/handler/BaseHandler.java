package com.findcab.driver.handler;

public class BaseHandler extends AbsHandler {

	@Override
	public Object parseResponse(String responseStr) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("-------responseStr-----------"+responseStr);
		return responseStr;

	}
}
