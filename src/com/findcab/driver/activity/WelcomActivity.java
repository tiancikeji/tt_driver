package com.findcab.driver.activity;

import cn.jpush.android.api.JPushInterface;

import com.findcab.R;
import com.findcab.driver.util.MyLogTools;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.RelativeLayout;

public class WelcomActivity extends Activity {
	RelativeLayout welcom_layout;
//	SharedPreferences sharedata = getSharedPreferences("data", 0);
	String name ;
	String password ;
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
		// welcom_layout.startAnimation(animationset);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent mainIntent = null;
				if (isSignup()) {
					Bundle mBundle = new Bundle();
					mBundle.putString("name", name);
					mBundle.putString("password", password);

					mainIntent = new Intent(WelcomActivity.this,
							LocationOverlay.class);
				mainIntent.putExtras(mBundle);
				} else {

					mainIntent = new Intent(WelcomActivity.this,
							LandActivity.class);
				}
				startActivity(mainIntent);
				finish();
			}

		}, 1000);
		
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
	

	// 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
	private void initJPush(){
		JPushInterface.init(getApplicationContext());
	}
}
