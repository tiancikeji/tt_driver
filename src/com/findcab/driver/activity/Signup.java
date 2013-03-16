package com.findcab.driver.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.MD5;
import com.findcab.driver.util.RandomValidateCode;
import com.findcab.driver.util.Tools;

/**
 * 注册
 * 
 * @author yuqunfeng
 * 
 */
public class Signup extends Activity implements OnClickListener {

	private Button back, start;
	private Button butt_verification;
	private EditText edit_name, edit_mobile, edit_password, edit_license;
	private String name, mobile, password, license;
	private CheckBox checkBox;
	private Context context;
	private Location location;
	private double lat;
	private double lng;
	private TextView item;
	private DriverInfo info;
	private String androidDevice;
	private String randomStr = null;
	private EditText edit_verification;
	private static String verificationCode = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		initView();
		initLocation();
		Tools.init();
	}

	private void initView() {
		context = this;
		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(this);
		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(this);

		edit_name = (EditText) findViewById(R.id.name);
		edit_mobile = (EditText) findViewById(R.id.mobile);
		edit_password = (EditText) findViewById(R.id.password);
		edit_license = (EditText) findViewById(R.id.car_license);
		edit_verification = (EditText) findViewById(R.id.edit_verification);
		butt_verification = (Button) findViewById(R.id.butt_verification);
		butt_verification.setOnClickListener(this);
		checkBox = (CheckBox) findViewById(R.id.checkBox);

		item = (TextView) findViewById(R.id.item);
		item.setOnClickListener(this);
		androidDevice = Tools.getDeviceId(context);
		if (initGPS()) {

			LocationManager locationManager;
			String serviceName = Context.LOCATION_SERVICE;
			locationManager = (LocationManager) this
					.getSystemService(serviceName);
			// 查询条件
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);

			String provider = locationManager.getBestProvider(criteria, true);
			location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				lat = location.getLatitude();
				lng = location.getLongitude();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.butt_verification:
			mobile = edit_mobile.getText().toString().trim();

			if (!mobile.equals("")) {

				sendSMS(mobile);
			} else {
				Tools.myToast(context, "请输入手机号码！");
			}
			break;
		case R.id.back:

			finish();
			break;
		case R.id.start:

			name = edit_name.getText().toString().trim();
			mobile = edit_mobile.getText().toString().trim();
			password = edit_password.getText().toString().trim();
			license = edit_license.getText().toString().trim();

			verificationCode = edit_verification.getText().toString().trim();

			if (verificationCode.toLowerCase().equals(randomStr.toLowerCase())) {
				// 验证码匹配成功
				if (!checkBox.isChecked()) {

					Toast.makeText(Signup.this, "请确认是否已经阅读同意条款！",
							Toast.LENGTH_SHORT).show();
					return;

				}

				if (!isComplete()) {

					Toast.makeText(Signup.this, "请完整填写内容！", Toast.LENGTH_SHORT)
							.show();
					return;
				}
				postInfo();

			} else if (verificationCode.equals("")) {
				Tools.myToast(context, "请输入验证码！");
			} else if (!verificationCode.toLowerCase().equals(
					randomStr.toLowerCase())) {

				Tools.myToast(context, "请输入正确验证码！");
			}

			break;
		case R.id.item:
			Tools
					.landDialog(
							context,
							"1注册条款的接受一旦会员在注册页面点击“我同意接受以上注册条款”后，这就表示会员已经阅读并且同意与第一眼相亲网网站达成协议，并接受所有的注册条款。",
							"注册条款");
			break;
		default:
			break;
		}
	}

	private boolean isComplete() {

		if (!name.equals("") && !mobile.equals("") && !password.equals("")
				&& !license.equals("")) {

			return true;

		}
		return false;

	}

	/**
	 * 提交信息
	 */
	private void postInfo() {
		if (HttpTools.checkNetWork(context)) {
			new Thread(new Runnable() {
				public void run() {

					Map<String, String> map = new HashMap<String, String>();
					MD5 md5 = new MD5();
					map.put("driver[name]", name);
					map.put("driver[mobile]", mobile);
					map.put("driver[password]", md5.getMD5ofStr(password));
					map.put("driver[car_license]", license);
					map.put("driver[car_type]", "car_type");
					map.put("driver[car_service_number]", "13812345678");
					map.put("driver[rate]", "10");
					map.put("driver[androidDevice]", androidDevice);
					if (lat == 0) {

						lat = 39.876757965948;
						lng = 116.65188108138;
						System.out.println("lat--->" + lat);
						System.out.println("lng--->" + lng);
					}

					map.put("driver[lat]", String.valueOf(lat));
					map.put("driver[lng]", String.valueOf(lng));

					try {
						// String result = (String) HttpTools.postAndParse(
						// Constant.DRIVERS_SIGNUP, map, new BaseHandler());
						String result = (String) HttpTools.PostDate(
								Constant.DRIVERS_SIGNUP, map);
						if (result != null) {

							JSONObject object = new JSONObject(result);
							info = new DriverInfo(object);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (info != null) {
						messageHandler.sendEmptyMessage(Constant.SUCCESS);

						return;
					} else {

						messageHandler.sendEmptyMessage(Constant.FAILURE);
					}

					return;
				}
			}).start();
		}
	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				Intent intent = new Intent(Signup.this, LandActivity.class);
				intent.putExtra("name", mobile);
				intent.putExtra("password", password);
				setResult(1, intent);
				finish();
				break;
			case Constant.FAILURE:
				Tools.landDialog(context, null, "注册失败");

				break;

			}
		}
	};

	private boolean initGPS() {
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// 判断GPS模块是否开启，如果没有则开启
		if (!locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS is not open,Please open it!",
					Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, 0);

			return false;
		} else {
			Toast.makeText(this, "GPS is ready", Toast.LENGTH_SHORT);
		}
		return true;
	}

	/**
	 * 初始化所在GPS位置
	 */
	private void initLocation() {

		LocationManager locationManager;
		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) this.getSystemService(serviceName);
		// 查询条件
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		if (location != null) {

			lat = location.getLatitude();
			lng = location.getLongitude();

		}
	}

	/**
	 * 发送验证信息
	 */
	private void sendSMS(String phone) {
		if (HttpTools.checkNetWork(context)) {
			randomStr = RandomValidateCode.getRandomString();
			Map<String, String> map = new HashMap<String, String>();
			String url = "http://www.smsbao.com/sms";

			MD5 md5 = new MD5();
			map.put("u", "fpwang");
			map.put("p", md5.getMD5ofStr("tiantiandache"));
			map.put("m", phone);
			map.put("c", randomStr);
			String result = HttpTools.PostDate(url, map);

			if (null != result && result.equals("0")) {

				Tools.myToast(context, "验证码已发送！");
			} else {
				Tools.myToast(context, "发送失败！");

			}

		}
	}
}
