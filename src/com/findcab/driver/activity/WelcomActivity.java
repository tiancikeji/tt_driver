package com.findcab.driver.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.findcab.R;
import com.findcab.driver.handler.BaseHandler;
import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.MD5;
import com.findcab.driver.util.MyLogTools;
import com.findcab.driver.util.Tools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.RelativeLayout;

public class WelcomActivity extends Activity implements BDLocationListener{
	RelativeLayout welcom_layout;
//	SharedPreferences sharedata = getSharedPreferences("data", 0);
	String name ;
	String password ;
	
	public ProgressDialog pd;
	protected String error;
	protected DriverInfo info;
	
	public static final int LAND_SUCCESS = 1;
	public static final int LAND_FAILURE = 2;
	
	private double lat;// 经度
	private double lng;// 纬度
	private LocationClient mLocClient;
	
	Handler messageHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case LAND_SUCCESS:
				Bundle mBundle = new Bundle();
				mBundle.putString("name", name);
				mBundle.putString("password", password);
				Intent intent1 = new Intent(WelcomActivity.this,LocationOverlay.class);
//				Intent intent1 = new Intent(WelcomActivity.this,MainMapActivity.class);
				intent1.putExtras(mBundle);
				startActivity(intent1);
				finish();
				break;
			case LAND_FAILURE:
				Intent intent2 = new Intent(WelcomActivity.this,LandActivity.class);
				startActivity(intent2);
				finish();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//初始化极光推送
		initJPush();
		
		//获取imei
		TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE); 
		String imei = tm.getDeviceId();
		MyLogTools.e("imei", ""+imei);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
		welcom_layout = (RelativeLayout) findViewById(R.id.welcom_layout);
		AnimationSet animationset = new AnimationSet(true);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
		alphaAnimation.setDuration(1100);
		animationset.addAnimation(alphaAnimation);
		//定位
		startLocation();
		
		// welcom_layout.startAnimation(animationset);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent mainIntent = null;
				if (isSignup()) {
					//如用户已登录过，则loading时进行登录
					land(name,password);
					
//					mainIntent = new Intent(WelcomActivity.this,
//							LocationOverlay.class);
				
				} else {

					mainIntent = new Intent(WelcomActivity.this,
							LandActivity.class);
					startActivity(mainIntent);
					finish();
				}
			}

		}, 1000);
		
	}

	@Override
	protected void onDestroy() {
		mLocClient.stop();
		super.onDestroy();
	}
	
	/**
	 * 得到用户是否注册
	 */
	private boolean isSignup() {
		SharedPreferences sharedata = getSharedPreferences("data", 0);
	
		 name = sharedata.getString("name", "");
		 password = sharedata.getString("password", "");
		if (!name.equals("") && !password.equals("")) {
			return true;
		}

		return false;
	}
	
	/**
	 * 登陆
	 */
	private void land(final String name, final String password) {
		pd = ProgressDialog.show(this, "", "正在登陆...", true, true);
		if (HttpTools.checkNetWork(this)) {

			new Thread(new Runnable() {

				public void run() {
						// 不为空

						Map<String, String> map = new HashMap<String, String>();
						MD5 md5 = new MD5();
						map.put("driver[mobile]", name);
						map.put("driver[password]", md5.getMD5ofStr(password));
						//定位成功
						if(lat>0){
							map.put("driver[lat]", String.valueOf(lat));
							map.put("driver[lng]", String.valueOf(lng));
							Log.e("定位2", lat+"-"+lng);
						}
						
						// String result = HttpTools.PostDate(
						// Constant.DRIVERS_SIGNIN, map);
						String result = (String) HttpTools
								.postAndParse(Constant.DRIVERS_SIGNIN, map,
										new BaseHandler());

						JSONObject jsonObject;
						try {

							if (result != null) {
								jsonObject = new JSONObject(result);
								if (jsonObject.has("error")) {

									error = jsonObject.getString("error");
									messageHandler
											.sendEmptyMessage(Constant.FAILURE);
									return;
								}

								JSONObject object = jsonObject
										.getJSONObject("driver");
								info = new DriverInfo(object);

							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						save(name, password);
						messageHandler.sendEmptyMessage(LAND_SUCCESS);

				}
			}).start();
		}
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if (resultCode == 1) {
//
//			name = data.getStringExtra("name");
//			password = data.getStringExtra("password");
//			land(name, password);
//		}
//	}

	/**
	 * 保存用户信息
	 */
	private void save(String name, String password) {
		Editor sharedata = getSharedPreferences("data", 0).edit();

		sharedata.putString("password", password);

		sharedata.putString("name", name);

		sharedata.commit();

	}
	
	private void startLocation(){
		mLocClient = new LocationClient(this);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		mLocClient.setLocOption(option);
		mLocClient.registerLocationListener(this);// 要实现BDLocationListener的接口
		mLocClient.start();
	}
	
	@Override
	public void onReceiveLocation(BDLocation location) {
		if (HttpTools.checkNetWork(this)) {

			lat = location.getLatitude();
			lng = location.getLongitude();
			Log.e("定位1", lat+"-"+lng);
		}
		
	}


	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub
		
	}

	// 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
	private void initJPush(){
		JPushInterface.init(getApplicationContext());
	}
}
