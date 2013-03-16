package com.findcab.driver.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.RelativeLayout;

public class WelcomActivity extends Activity {
	RelativeLayout welcom_layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
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

					mainIntent = new Intent(WelcomActivity.this,
							LocationOverlay.class);
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
		String name = sharedata.getString("name", "");
		String password = sharedata.getString("password", "");
		if (!name.equals("") && !password.equals("")) {
			return true;
		}

		return false;
	}

}
