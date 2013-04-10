package com.findcab.driver.util;

/**
 * @author xy
 * @date 2012-3-3
 */
public class Constant {
	/**
	 * 这个用来存储从信息对象
	 */
	public static final int SUCCESS = 111;
	public static final int FAILURE = 222;
	public static final int ERROR = 333;
	public static final int CHANGE = 444;
	public static final int CHANGE1 = 555;
	public static final int TRIPSUCCESS = 666;
	public static final int CHANGE2=777;
	public static final String BASEURL = "http://vissul.com:8989/api/";

	public static final String DRIVERS_SIGNUP = BASEURL + "drivers/signup/";// 司机注册(司机端)
	public static final String DRIVERS_SIGNIN = BASEURL + "drivers/signin/";// 司机登陆
	public static final String DRIVERS_PASSENGERS = BASEURL + "passengers/?";// 我附近得乘客
	public static final String CONVERSATIONS = BASEURL + "conversations/?";// 我附近得乘客
	public static final String CONVERSATIONS1 = BASEURL + "conversations/";
	public static final String TRIPS = BASEURL + "trips/";// 获得路线

	public static final String SIGNOUT = BASEURL + "drivers/signout/?";// 获得路线

	public static final String UPDATE = BASEURL + "drivers/?";// 获得路线
	
	public static final String VERSION_ID= "1.0.0";//版本号
}
