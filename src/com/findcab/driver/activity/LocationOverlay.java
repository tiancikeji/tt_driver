package com.findcab.driver.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.findcab.R;
import com.findcab.driver.adapter.AppointAdapter;
import com.findcab.driver.adapter.InfoAdapter;
import com.findcab.driver.handler.BaseHandler;
import com.findcab.driver.handler.ConversationsHandler;
import com.findcab.driver.handler.PassengersHandler;
import com.findcab.driver.object.ConversationInfo;
import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.object.PassengerInfo;
import com.findcab.driver.object.TripsInfo;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.MD5;
import com.findcab.driver.util.MyItemizedOverlay;
import com.findcab.driver.util.MyLogTools;
import com.findcab.driver.util.Tools;
import com.findcab.jpush.MyJpushTools;
import com.findcab.mywidget.MyToast;
import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.speech.SynthesizerPlayerListener;

public class LocationOverlay extends Activity implements OnClickListener,
BDLocationListener, SynthesizerPlayerListener {

	// 缓存对象.
	private SharedPreferences mSharedPreferences;

	// 合成对象.
	private SynthesizerPlayer mSynthesizerPlayer;

	// 弹出提示
	private Toast mToast;

	// 缓冲进度
	private int mPercentForBuffering = 0;

	// 播放进度
	private int mPercentForPlaying = 0;

	private LinearLayout linear_left;//地图左侧布局父view

	private LinearLayout linearlayout_conversation_info;//当前会话信息linearlayout
	private ImageView imageview_start;//会话信息中开始地点图标
	private ImageView imageview_end;//结束地点图标
	private ImageView imageview_distance;//距离图标
	private ImageView imageview_money;//加价图标
	private ImageView imageview_time;//时间退表
	private TextView textview_start;//起点
	private TextView textview_end;//终点
	private TextView textview_distance;//距离
	private TextView textview_money;//加价
	private TextView textview_time;//倒计时
	private TextView textview_distance_km;//距离单位-公里
	private TextView textview_money_rmb;//加价-元
	private TextView textview_time_second;//倒计时-秒
	private LinearLayout linearlayout_time;

	private Button btn_requestlist;//显示打车请求个数的按钮
	private ListView listView_request;//打车请求列表
	InfoAdapter myConversation_adapter;//打车请求适配器

	private Button button_response;//应答按钮
	private Button button_callphone;//打电话
	private Button button_passengergeton;//乘客以上车
	private LinearLayout linearlayout_response_after;//应答后，显示拨打电话和一上车按钮view

	public static MapView mMapView = null;

	private Button button_set;//设置
	private Button button_refurbish;//刷新按钮
	private Button button_road;//启动实时路况

	// 我的订单控件
	private LinearLayout linearlayout_myorders;
	private ListView listview_myorders;

	// 导航栏
	private LinearLayout linear_nav;
	private Button linear_nav_main, linear_nav_oppointment, linear_nav_order,linear_nav_set;

	// 预约界面
	private LinearLayout linear_oppointment;
	private Button linear_oppointment_all, linear_oppointment_today,
	linear_oppointment_tommorrow, linear_oppointment_long,
	linear_oppointment_near;

	// 预约列表
	private LinearLayout linear_oppointment_right;
	private ListView linear_oppointment_listview;

	protected int count = 0;
	private int biao = 0;//是否在等待应答状态
	private int lubiao = 0;//判断是否显示路标

	public Context context;

	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";//地图key
	BMapManager mBMapMan = null;//地图管理
	MapController mapController;

	List<GeoPoint> pList;
	TripsInfo tripsInfo = null;//用来保存当前显示路线信息

	List<TripsInfo> tripsList;//保存所有路线信息
	ConversationInfo conversationInfo;//保存当前会话信息
	List<ConversationInfo> listConversationInfo = new ArrayList<ConversationInfo>();//会话列表
	private List<PassengerInfo> listPassengerInfo;//保存乘客信息列表
	private DriverInfo info;//用户信息
	//	private OwnerInfo ownerInfo;//现在没用，以后会用它保存用户信息
	private int trip_id;
	
	int iZoom = 0;

	private int lat;
	private int lng;

	// 定位坐标
	private double latOnwer;//我的位置
	private double lngOnwer;

	public Bundle bundle;
	private boolean ispassengersrun = true;//轮询获取乘客信息
	private boolean isAnswer = false;// 是否应答
	
	private String androidDevice;

	private String id;
	private LocationClient mLocClient;//定位
	// 存放overlayitem
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	private String name;
	private String password;
	protected String error;
//	private boolean isStartCount = true;
	private boolean isWaiting =false;

	private final int WAITING = 90;

	/**
	 * handler消息列表
	 */
	static final int MESSAGE_CONVERSATIONS_CHANGE = 10001;
	static final int MESSAGE_DRIVERS_CHANGE = 10002;
	static final int MESSAGE_PASSAGERS_CHANGE = 10003;

	//	private int countbiao=0;
	TelephonyManager manager ; 

	// 处理所有LocationOverlay类中的handler异步处理
	Handler handlerMain = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CONVERSATIONS_CHANGE:
				//				Toast.makeText(context, "会话更新", Toast.LENGTH_SHORT).show();
				MyToast toast = new MyToast(context,"会话更新");
				toast.startMyToast();
				Log.e("刷新会话", "会话更新");
				getConversations();

				break;
			case MESSAGE_DRIVERS_CHANGE:
				//				Toast.makeText(context, "附近司机更新", Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_PASSAGERS_CHANGE:
				//				Toast.makeText(context, "附近乘客更新", Toast.LENGTH_SHORT).show();
				getPassengers();
				break;

			}
		}

	};

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		context = getApplicationContext();

		// 获取前一个activity传入的用户名密码（这里要修改）应保存起来
		bundle = this.getIntent().getExtras();
		name = bundle.getString("name");
		password = bundle.getString("password");
		tripsList = new ArrayList<TripsInfo>();

		// 初始化地图
		initManager();
		setContentView(R.layout.mapview2);
		Tools.init();// 检查网络，长时间请求问题

		initView();// 初始化控件
		mSharedPreferences = getSharedPreferences(getPackageName(),
				MODE_PRIVATE);

		mToast = Toast.makeText(this,
				String.format(getString(R.string.tts_toast_format), 0, 0),
				Toast.LENGTH_LONG);

		Map<String, String> map = new HashMap<String, String>();
		MD5 md5 = new MD5();
		map.put("driver[mobile]", name);
		map.put("driver[password]", md5.getMD5ofStr(password));
		//		hide();
		// String result = HttpTools.PostDate(
		// Constant.DRIVERS_SIGNIN, map);
		// 登陆
		String result = (String) HttpTools.postAndParse(
				Constant.DRIVERS_SIGNIN, map, new BaseHandler());

		// 这里在获取什么数据
		JSONObject jsonObject;
		try {

			if (result != null) {
				jsonObject = new JSONObject(result);
				if (jsonObject.has("error")) {

					error = jsonObject.getString("error");
					messageHandler.sendEmptyMessage(Constant.FAILURE);
					return;
				}

				JSONObject object = jsonObject.getJSONObject("driver");
				info = new DriverInfo(object);
				MyLogTools.e("LocationOverlay-UserInfo", info.getMobile() + "-"
						+ info.getId());

				// ownerInfo = new
				// OwnerInfo(object);//以后都用这个类，DriverInfo可以用来保存所有司机信息
				upDatedes();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 设置设备别名，以用户名为别名，（最终要实现当用户换账号时，别名改变）
		MyJpushTools.setAlias(context, "driver_" + info.getId());
		MyLogTools.e("别名", "driver_" + info.getId());

		//第一次进入程序，获取数据
		getConversations();
	}

	@Override
	protected void onStart() {
		super.onStart();
		initMyBroadcastReceiver();// 动态注册广播

	}

	@Override
	protected void onStop() {
		mToast.cancel();
		if (null != mSynthesizerPlayer) {
			mSynthesizerPlayer.cancel();
		}
		// unregisterReceiver(MyReceiver);// 当主activity关闭，广播关闭
		super.onStop();
	}

	/**
	 * 使用SynthesizerPlayer合成语音，不弹出合成Dialog.
	 * 
	 * @param
	 */
	public void synthetizeInSilence(String content) {
		// 创建合成对象.
		if (null == mSynthesizerPlayer) {
			mSynthesizerPlayer = SynthesizerPlayer.createSynthesizerPlayer(
					context, "appid=50ee7791");
		}

		// mSynthesizerPlayer.replay();

		// 设置合成发音人.
		// String role = mSharedPreferences.getString(context
		// .getString(R.string.preference_key_tts_role), context
		// .getString(R.string.preference_default_tts_role));
		mSynthesizerPlayer.setVoiceName("vixy");

		// 设置发音人语速
		int speed = mSharedPreferences.getInt(
				context.getString(R.string.preference_key_tts_speed), 50);
		mSynthesizerPlayer.setSpeed(speed);

		// 设置音量.
		// int volume = mSharedPreferences.getInt(context
		// .getString(R.string.preference_key_tts_volume), 50);
		mSynthesizerPlayer.setVolume(99);

		// 设置背景音.
		// String music = mSharedPreferences.getString(context
		// .getString(R.string.preference_key_tts_music), context
		// .getString(R.string.preference_default_tts_music));
		// mSynthesizerPlayer.setBackgroundSound(music);

		// 进行语音合成.
		mToast.setText(content);
		mToast.show();
		if (content != null) {

			mSynthesizerPlayer.playText(content, null, this);
			mToast.setText(String
					.format(context.getResources().getString(
							R.string.tts_toast_format), 0, 0));
			mToast.show();
		}

	}

	/**
	 * 初始化BMapManager
	 */
	private void initManager() {
		context = this;
		if (mBMapMan == null) {
			mBMapMan = new BMapManager(this);
		}
		mBMapMan.init(mStrKey, new MyGeneralListener());

	}

	@Override
	protected void onPause() {

		mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {

		mBMapMan.start();
		super.onResume();
	}

	class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
		}

		@Override
		public void onGetPermissionState(int iError) {
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {

			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		//可能为作废代码
//		case R.id.answer: // 应答
//			id = String.valueOf(conversationInfo.getId());
//			changeConversationsStatus("1", id);
//			// 这里的处理是错误的，应该是修改服务器成功后，将conversationInfo修改为1，以应答
//			conversationInfo.setStatus(1);
//			// answer.setVisibility(View.GONE);
//			isAnswer = true;
//			// getConversations();
//			break;
		
		//会有此功能，目前没有加入
//		case R.id.cancel:// 取消
//			changeConversationsStatus("2", id);
//			isAnswer = false;
//			conversationInfo = null;
//			// getConversations();
//			break;
		case R.id.button_callphone:// 拨打电话
			String moble = null;
			if (listPassengerInfo != null) {

				for (int i = 0; i < listPassengerInfo.size(); i++) {

					if (listPassengerInfo.get(i).getId() == conversationInfo
							.getFrom_id()) {
						moble = listPassengerInfo.get(i).getMobile();
					}

				}
				id = String.valueOf(conversationInfo.getId());
				changeConversationsStatus("4", id);

				TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
					Intent intent = new Intent(Intent.ACTION_CALL,
							Uri.parse("tel:" + moble));

					startActivity(intent);

				} else {
					//					Toast.makeText(context, "SIM卡没有或读取有误！", Toast.LENGTH_SHORT).show();
					MyToast toast = new MyToast(context,"SIM卡没有或读取有误！");
					toast.startMyToast();
				}
			}
			break;
		case R.id.button_passengergeton: // 乘客上车
			// linear_left.setVisibility(View.GONE);
			// showNorm();
			//这里id获取不到，可能是清空conversationInfo导致
			id = String.valueOf(conversationInfo.getId());
			changeConversationsStatus("4", id);//4代表乘客以上车
			conversationInfo.setStatus(4);
			conversationInfo = null;

			isAnswer = false;

			for (int i = 0; i < listConversationInfo.size(); i++) {
				if (listConversationInfo.get(i).getStatus() != 0)
					listConversationInfo.remove(i);

			}

			biao = 0;
			tripsList.clear();
			isWaiting = false;
			getPassengers();
			getConversations();
			//显示乘客以上车页面
//			showPassengerGetOnView();
			//乘客上车后，直接显示新会话
			showNewMessageView();
			break;
		case R.id.button_response://应答
			for (int i = 0; i < listConversationInfo.size(); i++) {
				if (listConversationInfo.get(i).getStatus() != 0)
					listConversationInfo.remove(i);
			}
			ConversationInfo conversationInfo1 = null;
			String map1 = "[to_id=" + String.valueOf(info.getId()) + "]";
			
			listConversationInfo = (List<ConversationInfo>) HttpTools.getAndParse(Constant.CONVERSATIONS, map1, new ConversationsHandler());
			isAnswer = false;
			int biaozhi = 1;
			// if (conversationInfo != null) {
			if (listConversationInfo.size() > 0) {
				for (int i = 0; i < listConversationInfo.size(); i++) {
					conversationInfo1 = listConversationInfo.get(i);

					if (conversationInfo1.getFrom_id() == conversationInfo.getFrom_id()) {
						biaozhi = 0;
						if (conversationInfo1.getStatus() == 0) {
							showResponseAfterView();//显示应答后会话信息
							id = String.valueOf(conversationInfo1.getId());
							changeConversationsStatus("1", id);
							conversationInfo1.setStatus(1);
							conversationInfo=conversationInfo1;
							isWaiting = false;
							synthetizeInSilence("应答成功，请尽快联系乘客");
							break;
						} else if (conversationInfo1.getStatus() == -1) {
							isWaiting = false;
							synthetizeInSilence("订单已经取消");
							tripsList.clear();
							conversationInfo = null;
//							isStartCount = true;
							getPassengers();
							getConversations();
							break;
						} else {
							isWaiting = false;
							synthetizeInSilence("订单已经被抢");
							break;
						}
					}
				}
				if (biaozhi == 1) {
					isWaiting = false;
					synthetizeInSilence("订单已经取消");
//					isStartCount = true;
					tripsList.clear();
					getPassengers();
					getConversations();
				}

			} else {
				isWaiting = false;
				synthetizeInSilence("订单已经取消");
//				isStartCount = true;
				tripsList.clear();
				//				countbiao=0;
				getPassengers();
				getConversations();
			}
			// if(conversationInfo1==null)
			// {
			// synthetizeInSilence("订单已经取消");
			// }
			// else if(conversationInfo1.getStatus()==0&&biaozhi==0){
			// showCalledView();
			// //ispassengersrun=false;
			// //isRun=false;
			// // new Plysounds(context).synthetizeInSilence("应答成功，请尽快联系乘客");
			// synthetizeInSilence("应答成功，请尽快联系乘客");
			// //ispassengersrun=true;
			// //isRun=true;
			// // getPassengers();
			// // getConversations();
			// }
			// else{
			// // new Plysounds(context).synthetizeInSilence("订单也被抢");
			// synthetizeInSilence("订单也被抢");
			// }
			break;
		case R.id.btn_requestlist:
			//显示打车请求列表
			showRequestListView();
			break;
		case R.id.btn_refurbish:
			tripsList.clear();
			tripsInfo = null;
			mGeoList.clear();
			OverlayItem item = new OverlayItem(new GeoPoint(lat, lng), "item1",
			"item1");
			Drawable maker = getResources().getDrawable(R.drawable.car);
			item.setMarker(maker);
			mGeoList.add(item);

			MyItemizedOverlay overlay = new MyItemizedOverlay(context, maker,
					mGeoList);

			List<Overlay> list = mMapView.getOverlays();
			if (list != null && list.size() > 0) {
				list.remove(0);
			}
			mMapView.getOverlays().add(0, overlay);

			mapController.animateTo(new GeoPoint(lat, lng), null);
			mMapView.refresh();
			//			countbiao=0;
			getPassengers();
			getConversations();
			mapController.animateTo(new GeoPoint(lat, lng), null);
			mMapView.refresh();
			break;
		case R.id.btn_road:
			if (lubiao == 0) {
				lubiao = 1;
				mMapView.setTraffic(true);
				mMapView.refresh();
			} else {
				lubiao = 0;
				mMapView.setTraffic(false);
				mMapView.refresh();

			}
			break;
		case R.id.linear_leftnav_appointment:
			AppointAdapter adapter1 = new AppointAdapter(context, tripsList);
			tripsList.size();
			adapter1.upDatas(tripsList);
			linear_oppointment_listview.setAdapter(adapter1);
			linear_left.setVisibility(View.GONE);
			mMapView.setVisibility(View.GONE);
			linear_oppointment.setVisibility(View.VISIBLE);
			linear_oppointment_right.setVisibility(View.VISIBLE);
			break;
		case R.id.linear_leftnav_main:
			linear_left.setVisibility(View.VISIBLE);
			mMapView.setVisibility(View.VISIBLE);
			linear_oppointment.setVisibility(View.GONE);
			linear_oppointment_right.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	/**
	 * 显示我的订单
	 */
	private void setMyConversationsView() {

	}

	/**
	 * 获得周围的乘客
	 */
	private void getPassengers() {
		MyLogTools.e("LocationOnverlay", "getPassengers()");
		new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {

				while (ispassengersrun) {

					try {

						String map1 = null;
						// double lat1 = ((double) lat) / 1000000;
						// double lng1 = ((double) lng) / 1000000;
						// map1 = "passenger[androidDevice]=" + androidDevice
						// + "&passenger[lat]=" + String.valueOf(lat1)
						// + "&passenger[lng]=" + String.valueOf(lng1);
						map1 = "passenger[androidDevice]=" + androidDevice
						+ "&passenger[lat]=" + String.valueOf(latOnwer)
						+ "&passenger[lng]=" + String.valueOf(lngOnwer);

						listPassengerInfo = (List<PassengerInfo>) HttpTools.getAndParse(
								Constant.DRIVERS_PASSENGERS, map1,
								new PassengersHandler());

						if (listPassengerInfo != null && listPassengerInfo.size() > 0) {
							// 李吉喆改，地图上只表示会话列表中的乘客位置，所以去刷新Constant.CHANGE2
							messageHandler.sendEmptyMessage(Constant.SUCCESS);//此消息没做处理
							//							messageHandler.sendEmptyMessage(Constant.CHANGE2);
						} else {
							// MyLogTools.e("获取附近乘客失败", "无乘客数据");
							messageHandler.sendEmptyMessage(Constant.FAILURE);
						}
						try {
							// 获取周围乘客一分钟刷新一次
							Thread.sleep(60000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						count++;
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						return;
					}
				}

			}

		}).start();
	}

	/**
	 * 得到发布路线
	 */
	private void getTrip(final int trip_id,final ConversationInfo cInfo) {//传入conversationInfo是为了保存会话对应的trip信息

		MyLogTools.e("LocationOnverlay", "getTrip()");
		new Thread(new Runnable() {
			public void run() {

				try {
					String result = (String) HttpTools.getAndParse(
							Constant.TRIPS, trip_id + "", new BaseHandler());
					JSONObject object = new JSONObject(result);

					tripsInfo = new TripsInfo(object.getJSONObject("trip"));
					tripsList.add(tripsInfo);

					InfoAdapter adapter = new InfoAdapter(context, tripsList);
					tripsList.size();
					adapter.upDatas(tripsList);
					listView_request.setAdapter(adapter);
					System.out.println("getTrip------>" + result);
					//					//李吉喆 ==判断获取到的路线是否是当前会话的路线信息，如果是则单独保存
					//					if(conversationInfo.getId() == info.getId()){
					//						tripsInfo = info;
					//					}
					//得到发布路线，这里处理的不对
					if (tripsInfo != null) {
						MyLogTools.e("获取trip成功", "获取trip成功");
						requestMessage.sendEmptyMessage(Constant.SUCCESS);
					}else{
						MyLogTools.e("获取trip无数据", "获取trip无数据");
					}

				} catch (JSONException e) {
					e.printStackTrace();
					MyLogTools.e("获取trip", "异常");
					requestMessage.sendEmptyMessage(Constant.FAILURE);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					MyLogTools.e("获取trip", "异常");
					requestMessage.sendEmptyMessage(Constant.FAILURE);
					return;
				}

			}

		}).start();
	}

	/**
	 * 我的会话（发送给我的请求）
	 */
	private void getConversations() {
		MyLogTools.e("LocationOnverlay", "getConversations()");
		new Thread(new Runnable() {

			@SuppressWarnings("unchecked")
			public void run() {
				String map1 = null;
				try {
					for (int i = 0; i < listConversationInfo.size(); i++) {
						if (listConversationInfo.get(i).getStatus() != 0)
							listConversationInfo.remove(i);
					}
					map1 = "[to_id=" + String.valueOf(info.getId()) + "]";
					if (!isAnswer) {//判断是否在应答状态
						MyLogTools.e("LocationOverlay-getConversation()", "1");
						listConversationInfo = (List<ConversationInfo>) HttpTools.getAndParse(
								Constant.CONVERSATIONS, map1,
								new ConversationsHandler());

					} else {
						MyLogTools.e("LocationOverlay-getConversation()", "2");
						if (listConversationInfo != null) {
							listConversationInfo = (List<ConversationInfo>) HttpTools
							.getAndParse(Constant.CONVERSATIONS, map1,
									new ConversationsHandler());
						}

					}

					if (listConversationInfo.size() > 0) {
						MyLogTools.e("获取回话列表成功", "列表 ="+listConversationInfo.size());
						//						show();
						for (int i = 0; i < listConversationInfo.size(); i++) {
							conversationInfo = listConversationInfo.get(i);

							int Status = conversationInfo.getStatus();
							if (Status == 0 && !isAnswer) {
								trip_id = conversationInfo.getTrip_id();
								getTrip(trip_id,listConversationInfo.get(i));
//								countBackwards();
							} else if (Status == -1 && isAnswer) {
								giveupHandler
								.sendEmptyMessage(Constant.SUCCESS);

							} else if (Status == 3 && isAnswer) {
								giveupHandler
								.sendEmptyMessage(Constant.SUCCESS);
								//								count2++;
							}
						}
					} else {
						messageHandler.sendEmptyMessage(Constant.FAILURE);
						////						hide();//无数据，隐藏
						//						//当获取数据为空时 ，清空当前tripInfo
						//						tripsInfo = null;
					}

				} catch (Exception e) {
					e.printStackTrace();
					MyLogTools.e("异常处理", "getConversations异常处理");
				}
				messageHandler.sendEmptyMessage(Constant.CHANGE2);

			}
		}).start();
	}

	//	private void getMyconversations(){
	//		new Thread(new Runnable() {
	//
	//			public void run() {
	//				 int map1;
	//				 ConversationInfo ConversationInfo1=null;
	//				while(isMyconversationRun) {     
	//					map1=conversationInfo.getId();
	//					ConversationInfo1 = (ConversationInfo) HttpTools
	//							.getAndParse(Constant.CONVERSATIONS1, map1,
	//									new ConversationHandler1());
	//					 if(ConversationInfo1.getStatus()==4){
	//					isMyconversationRun=false;
	//					messageHandler
	//					.sendEmptyMessage(Constant.FINISH);
	//				  }
	//					 //取消订单是第二版功能
	////					 else if(ConversationInfo1.getStatus()==-1){
	////						isMyconversationRun=false;
	////						messageHandler
	////						.sendEmptyMessage(Constant.QUXIAO);
	////					}
	//					 
	//					 
	////					else if(ConversationInfo1.getStatus()==4){
	////						isMyconversationRun=false;
	////						messageHandler
	////						.sendEmptyMessage(Constant.FINISH);
	////					}
	//					
	//					try {
	//						Thread.sleep(5000);
	//					} catch (InterruptedException e) {
	//						// TODO Auto-generated catch block
	//						e.printStackTrace();
	//					}
	//				}
	//			
	//				}
	//		
	//	}).start();
	//	}


	/**
	 * 更新会话状态
	 */
	private void changeConversationsStatus(final String status, final String id) {
		MyLogTools.e("LocationOnverlay", "changeConversationsStatus()");
		new Thread(new Runnable() {
			public void run() {
				try {

					Map<String, String> map = new HashMap<String, String>();

					String status_desc = "";
					if (status.equals("1")) {
						status_desc = "accept";
					} else if (status.equals("2")) {
						status_desc = "reject";
					} else if (status.equals("3")) {
						status_desc = "finish";
					} else if (status.equals("4")) {
						status_desc = "finish";
					}
					map.put("conversation[status]", status);
					map.put("conversation[status_desc]", status_desc);

					String result = (String) HttpTools.postAndParse(
							Constant.CONVERSATIONS1 + id + "/", map,
							new BaseHandler());

					if (result != null && status.equals("1")) {
						answerHandler.sendEmptyMessage(Constant.SUCCESS);
					} else if (result != null && status.equals("2")) {
						rejectHandler.sendEmptyMessage(Constant.SUCCESS);
					} else if (result != null && status.equals("3")) {
						fullHandler.sendEmptyMessage(Constant.SUCCESS);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return;
				}

			}
		}).start();
	}

	/**
	 * 更新客满状态
	 */
	private void upDate() {
		MyLogTools.e("LocationOnverlay", "upDate()");
		new Thread(new Runnable() {
			public void run() {
				try {

					Map<String, String> map = new HashMap<String, String>();

					if (lat != 0) {
						map.put("driver[status]", "1");
					}
					String result = null;
					if (info != null) {
						result = (String) HttpTools.postAndParse(
								Constant.UPDATE + String.valueOf(info.getId()),
								map, new BaseHandler());
					}

					if (result == null) {
						return;
					}
					try {
						JSONObject jsonObject = new JSONObject(result);
						JSONObject object = jsonObject.getJSONObject("driver");
						if (object != null) {

						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return;
				}

			}

		}).start();

	}

	/*
	 * 更新司机地点
	 */
	private void upDatedes() {
		MyLogTools.e("LocationOnverlay", "upDatedes()");
		new Thread(new Runnable() {
			public void run() {
				try {

					Map<String, String> map = new HashMap<String, String>();

					// if (lat != 0) {
					map.put("driver[lat]", lat + "");
					map.put("driver[lng]", lng + "");
					// }
					String result = null;
					if (info != null) {
						result = (String) HttpTools.postAndParse(
								Constant.UPDATE + String.valueOf(info.getId()),
								map, new BaseHandler());
					}

					if (result == null) {
						return;
					}
					// 下面 代码没有做任何具体操作！！！
					try {

						JSONObject jsonObject = new JSONObject(result);
						JSONObject object = jsonObject.getJSONObject("driver");
						if (object != null) {
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return;
				}

			}

		}).start();

	}

	/**
	 * 退出
	 */
	private void signOut() {

		if (HttpTools.checkNetWork(context)) {
			new Thread(new Runnable() {
				public void run() {
					try {
						// 不为空
						Map<String, String> map = new HashMap<String, String>();
						map.put("driver[androidDevice]", androidDevice);
						String map1 = "driver[androidDevice]=" + androidDevice;
						String resultString = (String) HttpTools.getAndParse(
								Constant.SIGNOUT, map1, new BaseHandler());
						// System.out.println("signOut------->" + resultString);
						JSONObject jsonObject = new JSONObject(resultString);
						if (jsonObject.has("message")) {
							// System.out.println("message--->"
							// + jsonObject.getString("message"));

							//							// 退出登录后要清楚缓存
							//							SharedPreferences share = getSharedPreferences(
							//									"data", 0);
							//							Editor editor = share.edit();
							//							editor.clear();
							//							editor.commit();

							exitPro(context);
							finish();
							return;
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						exitPro(context);
					}
					exitPro(context);
				}

			}).start();
		}

	}



//	/**
//	 * 显示正常状态
//	 */
//	private void showNorm() {
//		MyLogTools.e("LocationOnverlay", "showNorm()");
//		//		 layout_instruction.setVisibility(View.GONE);
//		//		 answer.setVisibility(View.VISIBLE);
//		//显示应答按钮，隐藏：呼叫乘客，和乘客以上车按钮
//
//
//		//李吉喆加入，显示订单信息
//		for(int i = 0; i < tripsList.size();i++){
//			if(tripsList.get(i).getId() == conversationInfo.getId()){
//				textview_start.setText(tripsInfo.getStart());
//				textview_end.setText(tripsInfo.getEnd());
//				if (tripsInfo.getAppointment().equals(null)) {
//					textview_money.setText(0);
//					textview_distance.setText("不详");
//				} else {
//					textview_money.setText(tripsInfo.getAppointment());
//					textview_distance.setText(tripsInfo.getAppointment());
//				}
//			}
//		}
//	}

//	/**
//	 * 显示应答状态
//	 */
//	private void showAnswer() {
//		MyLogTools.e("LocationOnverlay", "showAnswer()");
//		//李吉喆加入，显示订单信息
//		for(int i = 0; i < tripsList.size();i++){
//			if(tripsList.get(i).getId() == conversationInfo.getId()){
//				textview_start.setText(tripsInfo.getStart());
//				textview_end.setText(tripsInfo.getEnd());
//				if (tripsInfo.getAppointment().equals(null)) {
//					textview_money.setText(0);
//					textview_distance.setText("不详");
//				} else {
//					textview_money.setText(tripsInfo.getAppointment());
//					textview_distance.setText(tripsInfo.getAppointment());
//				}
//			}
//		}
//
//	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.CHANGE2:
				MyLogTools.e("LocationOnverlay","messageHandler-Constant.CHANGE2");
				List<Overlay> list1 = mMapView.getOverlays();
				list1.clear();
				Drawable marker = getResources().getDrawable(
						R.drawable.iconmarka); 
				marker.setBounds(0, 0, marker.getIntrinsicWidth(),
						marker.getIntrinsicHeight());
				//移除坐标点
				for (int i = 1; i < mGeoList.size(); i++) {
					mGeoList.remove(i);
				}
				// 这里是打补丁，不应该在这里来处理数据不存在情况！
				if (listPassengerInfo != null && listPassengerInfo.size() != 0 && listConversationInfo != null
						&& listConversationInfo.size() > 0) {
					int size = listPassengerInfo.size();
					OverlayItem item = null;
					int lat, lng;
					PassengerInfo info;
					ConversationInfo cinfo;
					Drawable maker = getResources().getDrawable(
							R.drawable.passenger);
					for (int i = 0; i < size; i++) {
						info = listPassengerInfo.get(i);

						// 获取有请求的乘客
						for (int j = 0; j < listConversationInfo.size(); j++) {
							cinfo = listConversationInfo.get(j);
							if (info.getId() == cinfo.getFrom_id()) {
								lat = (int) (info.getLat() * 1e6);
								lng = (int) (info.getLng() * 1e6);
								item = new OverlayItem(new GeoPoint(lat, lng),
										info.getName(), "");

								item.setMarker(maker);

								mGeoList.add(item);
							}
						}

					}
					mGeoList.size();

					MyItemizedOverlay overlay = new MyItemizedOverlay(context,
							maker, mGeoList);
					mMapView.getOverlays().add(overlay);
					mMapView.postInvalidate();
					mMapView.refresh();

				} else {
					// 无乘客数据
					MyLogTools.e("测试乘客数据是否存在", "无乘客数据");
					
				}

				// 目前先判断路线信息是否存在，这里不晚上，以后会修改
				if (tripsInfo != null) {
					synthetizeInSilence("你有一条从" + tripsInfo.getStart() + "到"
							+ tripsInfo.getEnd() + "搭车请求！");
					//						show();
					linear_left.setVisibility(View.VISIBLE);
					btn_requestlist.setText(listConversationInfo.size() + "条新的未读打车请求");

					// btn_request.setVisibility(View.GONE);
					//						if (biao == 0){
					if(conversationInfo.getCountDownTime() > 0){
						button_response.setVisibility(View.VISIBLE);
					}
					textview_start.setText(tripsInfo.getStart());
					textview_end.setText(tripsInfo.getEnd());
					if (tripsInfo.getAppointment().equals(null)) {
						textview_money.setText(0);
						textview_distance.setText("不详");
					} else {
						textview_money.setText(tripsInfo.getAppointment());
						textview_distance.setText(tripsInfo.getAppointment());
					}
					isWaiting = true;
					countBackwards();
//						//判断是否还有打车请求，如果有，则继续倒计时（这里有漏洞，倒计时不应改在这里显示，应该把倒计时和显示trip信息放到一起）
//						if(tripsList.size() >0){
//							MyLogTools.e("change2-if", ""+tripsList.size());
//							isWaiting = true;
//							waitingTime = WAITING;//此处去掉，在启动倒计时线程时获取倒计时
//							countBackwards();//倒计时不应该在这里启动，应该是conversation获取成功，且status为0，就启动
//						}else{
//							MyLogTools.e("change2-else", ""+tripsList.size());
//						}
//						if(countbiao==0)
//							countBackwards();

				} else {
					// 无路线信息
					MyLogTools.e("测试路线数据是否存在", "无数据");
					showNoConversationOfData();
					
//					btn_requestlist.setText("0条新的未读打车请求");
//					textview_start.setText("");
//					textview_end.setText("");
//					textview_money.setText("");
//					textview_distance.setText("");
//					textview_time.setText("");
//					button_response.setVisibility(View.GONE);
				}
				break;
//			case Constant.CHANGE:
//				MyLogTools.e("LocationOnverlay",
//				"messageHandler-Constant.CHANGE");
//				btn_requestlist.setText(R.string.noask);
//				linearlayout_conversation_info.setVisibility(View.GONE);
////				// btn_request.setVisibility(View.GONE);
////				button_response.setVisibility(View.GONE);
////				textview_start.setText(" ");
////				textview_end.setText(" ");
////				textview_money.setText(" ");
////				textview_distance.setText(" ");
////				textview_time.setText(" ");
//				// linear_time.setVisibility(View.GONE);
//				break;
			case Constant.SUCCESS:

				break;
			case Constant.FAILURE:
				if (count == 0) {
					Tools.myToast(context, "附近没有乘客哦");
				}

				break;

			}
		}
	};

	/**
	 * 获得请求
	 */
	Handler requestMessage = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				MyLogTools.e("获取路线数据成功，并显示", tripsInfo.getId()+"获取路线数据成功，并显示"+conversationInfo.getTrip_id());
				//李吉喆加入，显示订单信息
				if(tripsInfo.getId() == conversationInfo.getTrip_id()){
					textview_start.setText(tripsInfo.getStart());
					textview_end.setText(tripsInfo.getEnd());
					if (tripsInfo.getAppointment().equals(null)) {
						textview_money.setText(0);
						textview_distance.setText("不详");
					} else {
						textview_money.setText(tripsInfo.getAppointment());
						textview_distance.setText(tripsInfo.getAppointment());
					}
					isWaiting = true;
				}else{
					MyLogTools.e("tripsInfo.getId()不等于", "conversationInfo.getTrip_id()");
				}
				break;
			case Constant.FAILURE:
				MyToast toast = new MyToast(LocationOverlay.this,"获取打车请求路线失败");
				toast.startMyToast();
				break;

			}
		}
	};
	/**
	 * 拒绝成功
	 */
	Handler rejectHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
//				showNorm();
				//未完成
				showNewMessageView();
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};
	/**
	 * 满员
	 */
	Handler fullHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
//				showNorm();
				//未完成
				showNewMessageView();
				upDate();
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};
	/**
	 * 以应答状态
	 */
	Handler answerHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
//				showAnswer();
				showResponseAfterView();//回话状态为以应答，则显示应答后页面
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};

	/**
	 * 用户放弃了
	 */
	Handler giveupHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				isAnswer = false;
//				showNorm();
				//未完成
				showNewMessageView();
				if (conversationInfo.getStatus() == -1) {
					Tools.myToast(context, "乘客已取消打车");
				}
				if (conversationInfo.getStatus() == 4) {
					Tools.myToast(context, "乘客已上车");
				}
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};

	/**
	 * 保存用户信息
	 */
	private void save(String name, String password) {
		Editor sharedata = getSharedPreferences("data", 0).edit();
		sharedata.putString("password", password);
		sharedata.putString("name", name);
		sharedata.commit();

	}

	/**
	 * 得到用户
	 */
	private void getData() {
		SharedPreferences sharedata = getSharedPreferences("data", 0);
		name = sharedata.getString("name", "");
		password = sharedata.getString("password", "");

	}

	public static final int LAND = 1;// 登陆

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		MyLogTools.e("LocationOnverlay", "onActivityResult()");
		switch (requestCode) {

		case LAND:
			if (data != null) {
				if (data.hasExtra("name")) {
					name = data.getStringExtra("name");
				}
				if (data.hasExtra("password")) {
					password = data.getStringExtra("password");
				}
				Bundle bundle = data.getExtras();
				if (bundle != null) {
					info = (DriverInfo) bundle.getSerializable("DriverInfo");
					tripsList.clear();
					getConversations();
				}
				save(name, password);
			}
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 1, 1, "登出");
		menu.add(0, 2, 2, "返回");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == 1) {
			save("", "");
			signOut();
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitDialog(context);
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 退出
	 * 
	 * @param context
	 */
	public void exitPro(Context context) {

		// 杀死Application
		String packName = context.getPackageName();
		ActivityManager activityMgr = (ActivityManager) context
		.getSystemService(Context.ACTIVITY_SERVICE);
		activityMgr.restartPackage(packName);
		activityMgr.killBackgroundProcesses(packName);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public void exitDialog(final Context context) {
		MyLogTools.e("LocationOnverlay", "exitDialog()");
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		View aalayout = View.inflate(context, R.layout.exit, null);

		builder.setView(aalayout);
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				if (info != null) {
					signOut();
				} else {
					exitPro(context);
				}
			}
		});

		builder.create().show();
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		MyLogTools.e("LocationOnverlay", "onReceiveLocation()");
		// TODO Auto-generated method stub

		latOnwer = location.getLatitude();
		lngOnwer = location.getLongitude();

		lat = (int) (location.getLatitude() * 1e6);
		lng = (int) (location.getLongitude() * 1e6);

		OverlayItem item = new OverlayItem(new GeoPoint(lat, lng), "item1",
		"item1");
		Drawable maker = getResources().getDrawable(R.drawable.car);
		item.setMarker(maker);
		mGeoList.add(item);

		MyItemizedOverlay overlay = new MyItemizedOverlay(context, maker,
				mGeoList);

		List<Overlay> list = mMapView.getOverlays();
		if (list != null && list.size() > 0) {
			list.remove(0);
		}
		mMapView.getOverlays().add(0, overlay);

		mapController.animateTo(new GeoPoint(lat, lng), null);
		mMapView.refresh();

		getPassengers();

		getConversations();
	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub

	}

	private void initView() {
		MyLogTools.e("LocationOnverlay", "initView2()");

		//打电话
		manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE); 
		manager.listen(new PhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE); 

		//初始化会话布局
		linearlayout_conversation_info = (LinearLayout) findViewById(R.id.linearlayout_conversation_info);//显示回话信息view
		imageview_start=(ImageView)findViewById(R.id.imageView_start);
		imageview_end=(ImageView)findViewById(R.id.imageView_end);
		imageview_distance=(ImageView)findViewById(R.id.imageView_distence);
		imageview_money=(ImageView)findViewById(R.id.imageView_money);
		imageview_time=(ImageView)findViewById(R.id.imageView_time);
		linearlayout_time = (LinearLayout) findViewById(R.id.linearlayout_time);
		textview_start = (TextView) findViewById(R.id.textview_start);
		textview_end = (TextView) findViewById(R.id.textview_end);
		textview_money = (TextView) findViewById(R.id.textview_money);
		textview_distance = (TextView) findViewById(R.id.textview_distance);
		textview_time = (TextView) findViewById(R.id.textview_time);
		textview_distance_km=(TextView)findViewById(R.id.textview_distance_km);
		textview_money_rmb=(TextView)findViewById(R.id.textview_money_rmb);
		textview_time_second=(TextView)findViewById(R.id.textView_time_second);

		//打车请求按钮
		btn_requestlist = (Button) findViewById(R.id.btn_requestlist);
		btn_requestlist.setOnClickListener(this);
		btn_requestlist.setText(R.string.noask);
		//打车请求列表
		listView_request = (ListView) findViewById(R.id.listView_request);
		myConversation_adapter = new InfoAdapter(context, tripsList);
		listView_request.setAdapter(myConversation_adapter);
		//应答按钮
		button_response = (Button) findViewById(R.id.button_response);
		button_response.setOnClickListener(this);
		button_response.setVisibility(View.GONE);

		//应答后按钮布局
		linearlayout_response_after = (LinearLayout) findViewById(R.id.linearlayout_response_after);
		button_callphone = (Button) findViewById(R.id.button_callphone);
		button_callphone.setOnClickListener(this);
		button_passengergeton = (Button) findViewById(R.id.button_passengergeton);
		button_passengergeton.setOnClickListener(this);

		button_refurbish = (Button) findViewById(R.id.btn_refurbish);
		button_refurbish.setOnClickListener(this);
		button_road = (Button) findViewById(R.id.btn_road);
		button_road.setOnClickListener(this);

		androidDevice = Tools.getDeviceId(context);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setDoubleClickZooming(false);
		mMapView.setClickable(false);
		mapController = mMapView.getController();
		mapController.setZoom(14);


		linear_left = (LinearLayout) findViewById(R.id.linear_left);



		mLocClient = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		mLocClient.setLocOption(option);
		mLocClient.registerLocationListener(this);
		mLocClient.start();


		// 导航栏
		linear_nav = (LinearLayout) findViewById(R.id.linear_leftnav);
		linear_nav_main = (Button) findViewById(R.id.linear_leftnav_main);
		linear_nav_main.setOnClickListener(this);
		linear_nav_oppointment = (Button) findViewById(R.id.linear_leftnav_appointment);
		linear_nav_oppointment.setOnClickListener(this);
		linear_nav_order = (Button) findViewById(R.id.linear_leftnav_order);
		linear_nav_order.setOnClickListener(this);

		// 预约界面

		linear_oppointment = (LinearLayout) findViewById(R.id.linear_oppointment_middle);
		linear_oppointment_all = (Button) findViewById(R.id.linear_oppointment_middle_all);
		linear_oppointment_today = (Button) findViewById(R.id.linear_oppointment_middle_today);
		linear_oppointment_tommorrow = (Button) findViewById(R.id.linear_oppointment_middle_tomorrow);
		linear_oppointment_long = (Button) findViewById(R.id.linear_oppointment_middle_long);
		linear_oppointment_near = (Button) findViewById(R.id.linear_oppointment_middle_near);

		linear_oppointment_all.setOnClickListener(this);
		linear_oppointment_today.setOnClickListener(this);
		linear_oppointment_tommorrow.setOnClickListener(this);
		linear_oppointment_long.setOnClickListener(this);
		linear_oppointment_near.setOnClickListener(this);

		// 预约listview
		linear_oppointment_right = (LinearLayout) findViewById(R.id.linear_oppointment_right);

		linear_oppointment_listview = (ListView) findViewById(R.id.listView_oppointment);

		// 我的订单控件初始化
		linearlayout_myorders = (LinearLayout) findViewById(R.id.mapview2_linearlayout_myorders);
		listview_myorders = (ListView) linearlayout_myorders
		.findViewById(R.id.my_orders_listview);

		listView_request.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int tem = arg2;
//				listView_request.setVisibility(View.GONE);
//				linearlayout_conversation_info.setVisibility(View.VISIBLE);
//				btn_requestlist.setVisibility(View.VISIBLE);
//				textview_start.setText(tripsList.get(arg2).getStart());
//				textview_end.setText(tripsList.get(arg2).getEnd());
//				textview_distance.setText(tripsList.get(arg2).getAppointment());
//				textview_money.setText(tripsList.get(arg2).getAppointment());
				conversationInfo = listConversationInfo.get(arg2);
				conversationInfo.getFrom_id();
				tripsInfo = tripsList.get(arg2);
				showNewMessageView();
				biao = 0;
				synthetizeInSilence("你有一条从" + tripsInfo.getStart() + "到"
						+ tripsInfo.getEnd() + "搭车请求！");
			}

		});
		getData();

	}


	private class PhoneCallListener extends PhoneStateListener {
		private boolean bphonecalling = false;

		@Override
		public void onCallStateChanged(int state, String incomingnumber) {
			// seems the incoming number is this call back always ""
			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				bphonecalling = true;
			} else if (TelephonyManager.CALL_STATE_IDLE == state
					&& bphonecalling) {
				//	                if (manager != null) {
				//	                	manager.listen(mPhoneCallListener,
				//	                            PhoneStateListener.LISTEN_NONE);
				//	                }
				bphonecalling = false; 

				//	                Intent i = getPackageManager().getLaunchIntentForPackage(
				//	                        getPackageName());
				Intent intent=new Intent();  

				intent.setClass(LocationOverlay.this,LocationOverlay.class);  
				startActivity(intent);  

				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
			super.onCallStateChanged(state, incomingnumber);
		}
	}

//	

	//	private void hide()
	//	{
	//		image1.setVisibility(View.GONE);
	//		image2.setVisibility(View.GONE);
	//		image3.setVisibility(View.GONE);
	//		image4.setVisibility(View.GONE);
	//		image5.setVisibility(View.GONE);
	//		text1.setVisibility(View.GONE);
	//		text2.setVisibility(View.GONE);
	//		text3.setVisibility(View.GONE);
	//	}
	//	private void show()
	//	{
	//		image1.setVisibility(View.VISIBLE);
	//		image2.setVisibility(View.VISIBLE);
	//		image3.setVisibility(View.VISIBLE);
	//		image4.setVisibility(View.VISIBLE);
	//		image5.setVisibility(View.VISIBLE);
	//		text1.setVisibility(View.VISIBLE);
	//		text2.setVisibility(View.VISIBLE);
	//		text3.setVisibility(View.VISIBLE);
	//	}

	/**
	 * 倒计时
	 */
	private void countBackwards() {
		MyLogTools.e("LocationOverlay-contBackwards", ""+conversationInfo.getCountDownTime());
		if (!isWaiting) {//不是在等待应答状态，此时可以倒计时
//			isStartCount = false;
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isWaiting) {
						try {
							Thread.currentThread().sleep(1000);
							Message msg = new Message();
							//							waitingTime--;
							conversationInfo.setCountDownTime(conversationInfo.getCountDownTime()-1);
							msg.what = WAITING;
							MyLogTools.e("LocationOverlay-contBackwards", ""+conversationInfo.getCountDownTime());
							waitingHandler.sendMessage(msg);

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
//					isStartCount = true;
				}
			}).start();
		}
	}

	Handler waitingHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WAITING:
				MyLogTools.e("LocationOverlay-waitingHandler", ""+conversationInfo.getCountDownTime());

				if (conversationInfo.getCountDownTime() >= 0) {
					textview_time.setText("" + conversationInfo.getCountDownTime());
					break;
				} else if (conversationInfo.getCountDownTime() == -1) {
					textview_time.setText("" + conversationInfo.getCountDownTime());
					//超时后，删除当前会话
					tripsInfo = null;
					conversationInfo = null;
					isWaiting = false;
					conversationInfo.setCountDownTime(90);//倒计时结束后不再把时间设置成90，不在计时
					showTimeOutDialog();
					break;
				}

			}

		}
	};

	//	/**
	//	 * 获取当前时间
	//	 */
	//	private double getDateAfterFormat_(String create_at){
	//		//2013-04-25T18:11:38+08:00
	//		
	//		double countDownTime = 0;
	//		String[] a = create_at.split("T");//截取出年月日和时分秒
	//		String[] b = a[0].split("-");//截取年月日
	//		String[] c = a[1].split("+");
	//		String[] d = c[0].split(":");//截取时分秒
	//		
	//		int year = Integer.valueOf(b[0]);
	//		int month = Integer.valueOf(b[1]);
	//		int day = Integer.valueOf(b[2]);
	//		int hourOfDay = Integer.valueOf(d[0]);
	//		int minute = Integer.valueOf(d[1]);
	//		int second = Integer.valueOf(d[2]);
	//		
	//		Calendar calendar_create_at = Calendar.getInstance();
	//		//月份-1是因为calendar中保存的月份是从0开始的
	//		calendar_create_at.set(year, month-1, day, hourOfDay, minute, second);
	//		
	//		Calendar calendar_now = Calendar.getInstance();
	//		calendar_now.set(calendar_now.get(Calendar.YEAR), calendar_now.get(Calendar.MONTH), calendar_now.get(Calendar.DAY_OF_MONTH), calendar_now.get(Calendar.HOUR_OF_DAY), calendar_now.get(Calendar.MINUTE), calendar_now.get(Calendar.SECOND));
	//		calendar_now.set(Calendar.MILLISECOND, 0);
	////		countDownTime = calendar_create_at.compareTo(calendar_now);
	//		String count1 = String.format("%1$04d%2$02d%3$02d%4$02d%5$02d%6$02d", calendar_now.get(Calendar.YEAR),calendar_now.get(Calendar.MONTH),calendar_now.get(Calendar.DAY_OF_MONTH),calendar_now.get(Calendar.HOUR_OF_DAY),calendar_now.get(Calendar.MINUTE),calendar_now.get(Calendar.SECOND));
	//		String count2 = String.format("%1$04d%2$02d%3$02d%4$02d%5$02d%6$02d", year,month,day,hourOfDay,minute,second);
	//		countDownTime = Double.compare(Double.valueOf(count2), Double.valueOf(count1));
	//		
	//		MyToast toast = new MyToast(context,":"+countDownTime);
	//		toast.startMyToast();
	//		if(countDownTime>0){
	//			return countDownTime;
	//		}else{
	//			return countDownTime;
	//		}
	//	}

	/**
	 * 显示TimeOutDialog zhaochuan
	 */
	private void showTimeOutDialog() {
		synthetizeInSilence("该订单已经超时");
		tripsList.clear();
		tripsInfo = null;
		conversationInfo = null;
		messageHandler.sendEmptyMessage(Constant.CHANGE2);
		getConversations();
	}
	
	/**
	 * 显示新消息，等待应答会话
	 */
	private void showNewMessageView() {
		MyLogTools.e("LocationOnverlay", "showNewMessage()");
		linearlayout_conversation_info.setVisibility(View.VISIBLE);//当前会话信息linearlayout,显示
		button_response.setVisibility(View.VISIBLE);//应答按钮
		//显示等待应答时，打电话和乘客上车按钮隐藏
		linearlayout_response_after.setVisibility(View.GONE);//应答后，显示拨打电话和一上车按钮view
		linearlayout_time.setVisibility(View.VISIBLE);
		
		btn_requestlist.setVisibility(View.VISIBLE);//显示打车请求个数的按钮
		btn_requestlist.setText(listConversationInfo.size() + "条新的未读打车请求");
		
		textview_start.setText(tripsInfo.getStart());
		textview_end.setText(tripsInfo.getEnd());
		textview_money.setText(tripsInfo.getAppointment());
		textview_distance.setText(tripsInfo.getAppointment());
		
//		//李吉喆加入，显示订单信息
//		for(int i = 0; i < tripsList.size();i++){
//			if(tripsList.get(i).getId() == conversationInfo.getId()){
//				textview_start.setText(tripsInfo.getStart());
//				textview_end.setText(tripsInfo.getEnd());
//				if (tripsInfo.getAppointment().equals(null)) {
//					textview_money.setText(0);
//					textview_distance.setText("不详");
//				} else {
//					textview_money.setText(tripsInfo.getAppointment());
//					textview_distance.setText(tripsInfo.getAppointment());
//				}
//			}
//		}
	}

//	/**
//	 * 用户点击应答，应答成功，等待乘客上车
//	 */
//	private void showPassengerGetOnView(){
//		btn_requestlist.setVisibility(View.VISIBLE);
//		btn_requestlist.setText(listConversationInfo.size() + "条新的未读打车请求");
//		linearlayout_response_after.setVisibility(View.VISIBLE);//应答后，显示，乘客上车和打电话按钮
//
//		textview_start.setText(tripsInfo.getStart());
//		textview_end.setText(tripsInfo.getEnd());
//		textview_money.setText(tripsInfo.getAppointment());
//		textview_distance.setText(tripsInfo.getAppointment());
//
//		InfoAdapter adapter = new InfoAdapter(context, tripsList);
//		tripsList.size();
//		adapter.upDatas(tripsList);
//		listView_request.setAdapter(adapter);
//	}

	/**
	 * 应答后页面显示
	 */
	private void showResponseAfterView() {
		linearlayout_conversation_info.setVisibility(View.VISIBLE);
		btn_requestlist.setVisibility(View.GONE);
		button_response.setVisibility(View.GONE);
		linearlayout_response_after.setVisibility(View.VISIBLE);
		linearlayout_time.setVisibility(View.GONE);//隐藏倒计时

		biao = 1;
		
		textview_start.setText(tripsInfo.getStart());
		textview_end.setText(tripsInfo.getEnd());
		textview_money.setText(tripsInfo.getAppointment());
		textview_distance.setText(tripsInfo.getAppointment());
		tripsList.clear();
		getPassengers();
		getConversations();
	}

	/**
	 * 显示打车请求列表
	 */
	private void showRequestListView() {
		if (listConversationInfo.size() > 0) {
			listView_request.setVisibility(View.VISIBLE);
			linearlayout_conversation_info.setVisibility(View.GONE);
			btn_requestlist.setVisibility(View.GONE);

			InfoAdapter adapter = new InfoAdapter(context, tripsList);
			tripsList.size();
			adapter.upDatas(tripsList);
			listView_request.setAdapter(adapter);
		}else{
			MyToast toast = new MyToast(context,"抱歉目前没有应答");
			toast.startMyToast();
		}
	}
	
	/**
	 * 显示无打车请求是页面
	 */
	private void showNoConversationOfData(){
		linearlayout_conversation_info.setVisibility(View.GONE);
		btn_requestlist.setText(R.string.noask);
	}
	

	/**
	 * SynthesizerPlayerListener的"播放进度"回调接口.
	 * 
	 * @param percent
	 *            ,beginPos,endPos
	 */
	@Override
	public void onBufferPercent(int percent, int beginPos, int endPos) {
		mPercentForBuffering = percent;
		mToast.setText(String.format(getString(R.string.tts_toast_format),
				mPercentForBuffering, mPercentForPlaying));
		mToast.show();
	}

	@Override
	public void onEnd(SpeechError arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayBegin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayPaused() {
		// TODO Auto-generated method stub

	}

	/**
	 * SynthesizerPlayerListener的"播放进度"回调接口.
	 * 
	 * @param percent
	 *            ,beginPos,endPos
	 */
	@Override
	public void onPlayPercent(int percent, int beginPos, int endPos) {
		mPercentForPlaying = percent;
		mToast.setText(String.format(getString(R.string.tts_toast_format),
				mPercentForBuffering, mPercentForPlaying));
		mToast.show();
	}

	@Override
	public void onPlayResumed() {
		// TODO Auto-generated method stub

	}

	/**
	 * 推送广播接收器
	 */
	private void initMyBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("cn.jpush.android.intent.REGISTRATION");// 用户注册SDK的intent
		filter.addAction("cn.jpush.android.intent.UNREGISTRATION");
		filter.addAction("cn.jpush.android.intent.MESSAGE_RECEIVED");// 用户接收SDK消息的intent
		filter.addAction("cn.jpush.android.intent.NOTIFICATION_RECEIVED");// 用户接收SDK通知栏信息的intent
		filter.addAction("cn.jpush.android.intent.NOTIFICATION_OPENED");// 用户打开自定义通知栏的intent
		filter.addCategory("com.findcab");
		getApplicationContext().registerReceiver(MyReceiver, filter); // 注册

	}

	/**
	 * 自定义广播,动态注册
	 */
	private BroadcastReceiver MyReceiver = new BroadcastReceiver() {
		private static final String TAG = "MyReceiver";

		// Bundle[{cn.jpush.android.NOTIFICATION_CONTENT_TITLE=天天打车—司机,
		// cn.jpush.android.NOTIFICATION_ID=149881293,
		// cn.jpush.android.ALERT=conversations}]
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();

			if (bundle.containsKey("cn.jpush.android.ALERT")) {
				MyLogTools.e(
						TAG,
						"接收到推送下来的自定义消息: "
						+ bundle.getString("cn.jpush.android.ALERT"));

				// 判断收到消息内容
				refreshViewByJPushInfo(bundle
						.getString("cn.jpush.android.ALERT"));
			}

		}

		// 打印所有的 intent extra 数据
		private String printBundle(Bundle bundle) {
			StringBuilder sb = new StringBuilder();
			for (String key : bundle.keySet()) {
				if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
					sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
				} else {
					sb.append("\nkey:" + key + ", value:"
							+ bundle.getString(key));
				}
			}
			return sb.toString();
		}
	};

	// 通过推送消息内容判断刷新内容，并通知handler操作;1,conversation_change 2,drivers_change
	// 3,passagers_change
	private void refreshViewByJPushInfo(String text) {
		if (text.equals("conversations")) {
			Log.e("收到推送", "conversations");
			handlerMain.sendEmptyMessage(MESSAGE_CONVERSATIONS_CHANGE);
		} else if (text.equals("drivers")) {
			handlerMain.sendEmptyMessage(MESSAGE_DRIVERS_CHANGE);
		} else if (text.equals("passagers")) {
			handlerMain.sendEmptyMessage(MESSAGE_PASSAGERS_CHANGE);
		} else {

		}
	}
}

//class OverlayTest extends ItemizedOverlay<OverlayItem> {
//	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
//	private Context mContext = null;
//	static PopupOverlay pop = null;
//
//	public OverlayTest(Drawable marker, Context context) {
//		super(marker);
//		this.mContext = context;
//		pop = new PopupOverlay(LocationOverlay.mMapView,
//				new PopupClickListener() {
//
//			@Override
//			public void onClickedPopup() {
//				MyLogTools.d("hjtest  ", "clickpop");
//			}
//		});
//		populate();
//
//	}
//
//	protected boolean onTap(int index) {
//		Drawable marker = this.mContext.getResources().getDrawable(
//				R.drawable.ic_launcher); // 得到需要标在地图上的资源
//		BitmapDrawable bd = (BitmapDrawable) marker;
//		Bitmap popbitmap = bd.getBitmap();
//		pop.showPopup(popbitmap, mGeoList.get(index).getPoint(), 32);
//		// int latspan = this.getLatSpanE6();
//		// int lonspan = this.getLonSpanE6();
//		Toast.makeText(this.mContext, mGeoList.get(index).getTitle(),
//				Toast.LENGTH_SHORT).show();
//		super.onTap(index);
//		return false;
//	}
//
//	public boolean onTap(GeoPoint pt, MapView mapView) {
//		if (pop != null) {
//			pop.hidePop();
//		}
//		super.onTap(pt, mapView);
//		return false;
//	}
//
//	@Override
//	protected OverlayItem createItem(int i) {
//		return mGeoList.get(i);
//	}
//
//	@Override
//	public int size() {
//		return mGeoList.size();
//	}
//
//	public void addItem(OverlayItem item) {
//		mGeoList.add(item);
//		populate();
//	}
//
//	public void removeItem(int index) {
//		mGeoList.remove(index);
//		populate();
//	}
//
//}

//不用的监听
//@SuppressWarnings("unchecked")
//public void onClick2(View v) {
//	// TODO Auto-generated method stub
//	switch (v.getId()) {
//	case R.id.button_passengergeton: // 乘客上车
//		// linear_left.setVisibility(View.GONE);
//		// showNorm();
//		//这里id获取不到，可能是清空conversationInfo导致
//		id = String.valueOf(conversationInfo.getId());
//		changeConversationsStatus("4", id);//4代表乘客以上车
//		conversationInfo.setStatus(4);
//		conversationInfo = null;
//
//		isAnswer = false;
//
//		for (int i = 0; i < listConversationInfo.size(); i++) {
//			if (listConversationInfo.get(i).getStatus() != 0)
//				listConversationInfo.remove(i);
//
//		}
//
//		biao = 0;
//		tripsList.clear();
//		isWaiting = false;
//		getPassengers();
//		getConversations();
//		//显示乘客以上车页面
//		showPassengerGetOnView();
//		break;
//	case R.id.button_response://应答
//		for (int i = 0; i < listConversationInfo.size(); i++) {
//			if (listConversationInfo.get(i).getStatus() != 0)
//				listConversationInfo.remove(i);
//
//		}
//		ConversationInfo conversationInfo1 = null;
//		String map1 = "[to_id=" + String.valueOf(info.getId()) + "]";
//
//		listConversationInfo = (List<ConversationInfo>) HttpTools.getAndParse(
//				Constant.CONVERSATIONS, map1, new ConversationsHandler());
//
//		int biaozhi = 1;
//		// if (conversationInfo != null) {
//		if (listConversationInfo.size() > 0) {
//			for (int i = 0; i < listConversationInfo.size(); i++) {
//				conversationInfo1 = listConversationInfo.get(i);
//
//				if (conversationInfo1.getFrom_id() == conversationInfo.getFrom_id()) {
//					biaozhi = 0;
//					if (conversationInfo1.getStatus() == 0) {
//						showCalledAfterView();//显示应答后会话信息
//						id = String.valueOf(conversationInfo1.getId());
//						changeConversationsStatus("1", id);
//						conversationInfo1.setStatus(1);
//						conversationInfo=conversationInfo1;
//
//						//							getTrip(conversationInfo1.getId());//当应答后，从新获取路线信息
//
//
//
//						isWaiting = false;
//						//							waitingTime = 60;//此处去掉，在启动倒计时线程时获取倒计时
//						// ispassengersrun=false;
//						// isRun=false;
//						synthetizeInSilence("应答成功，请尽快联系乘客");
//						isMyconversationRun=true;
//						//								getMyconversations();
//						break;
//						// ispassengersrun=true;
//						// isRun=true;
//						// getPassengers();
//						// getConversations();
//					} else if (conversationInfo1.getStatus() == -1) {
//						// new
//						// Plysounds(context).synthetizeInSilence("订单也被抢");
//						isWaiting = false;
//						synthetizeInSilence("订单已经取消");
//						tripsList.clear();
//						conversationInfo = null;
//						isStartCount = true;
//						//							countbiao=0;
//						getPassengers();
//						getConversations();
//						break;
//					} else {
//						isWaiting = false;
//						synthetizeInSilence("订单已经被抢");
//						break;
//					}
//					// if(conversationInfo.equals(conversationInfo1)){
//					// conversationInfo1=conversationInfo;
//
//				}
//
//			}
//			if (biaozhi == 1) {
//				isWaiting = false;
//				synthetizeInSilence("订单已经取消");
//				isStartCount = true;
//				tripsList.clear();
//				//					countbiao=0;
//				getPassengers();
//				getConversations();
//			}
//
//		} else {
//			isWaiting = false;
//			synthetizeInSilence("订单已经取消");
//			isStartCount = true;
//			tripsList.clear();
//			//				countbiao=0;
//			getPassengers();
//			getConversations();
//		}
//		// if(conversationInfo1==null)
//		// {
//		// synthetizeInSilence("订单已经取消");
//		// }
//		// else if(conversationInfo1.getStatus()==0&&biaozhi==0){
//		// showCalledView();
//		// //ispassengersrun=false;
//		// //isRun=false;
//		// // new Plysounds(context).synthetizeInSilence("应答成功，请尽快联系乘客");
//		// synthetizeInSilence("应答成功，请尽快联系乘客");
//		// //ispassengersrun=true;
//		// //isRun=true;
//		// // getPassengers();
//		// // getConversations();
//		// }
//		// else{
//		// // new Plysounds(context).synthetizeInSilence("订单也被抢");
//		// synthetizeInSilence("订单也被抢");
//		// }
//		break;
//	case R.id.btn_requestlist:
//		showPassengerListView();
//		break;
//	case R.id.btn_refurbish:
//		tripsList.clear();
//		tripsInfo = null;
//		mGeoList.clear();
//		OverlayItem item = new OverlayItem(new GeoPoint(lat, lng), "item1",
//		"item1");
//		Drawable maker = getResources().getDrawable(R.drawable.car);
//		item.setMarker(maker);
//		mGeoList.add(item);
//
//		MyItemizedOverlay overlay = new MyItemizedOverlay(context, maker,
//				mGeoList);
//
//		List<Overlay> list = mMapView.getOverlays();
//		if (list != null && list.size() > 0) {
//			list.remove(0);
//		}
//		mMapView.getOverlays().add(0, overlay);
//
//		mapController.animateTo(new GeoPoint(lat, lng), null);
//		mMapView.refresh();
//		//			countbiao=0;
//		getPassengers();
//		getConversations();
//		mapController.animateTo(new GeoPoint(lat, lng), null);
//		mMapView.refresh();
//		break;
//	case R.id.btn_road:
//		if (lubiao == 0) {
//			lubiao = 1;
//			mMapView.setTraffic(true);
//			mMapView.refresh();
//		} else {
//			lubiao = 0;
//			mMapView.setTraffic(false);
//			mMapView.refresh();
//
//		}
//		break;
//
//	case R.id.button_callphone:
//		String moble = null;
//
//		if (listPassengerInfo != null) {
//
//			for (int i = 0; i < listPassengerInfo.size(); i++) {
//
//				if (listPassengerInfo.get(i).getId() == conversationInfo
//						.getFrom_id()) {
//					moble = listPassengerInfo.get(i).getMobile();
//				}
//
//			}
//			id = String.valueOf(conversationInfo.getId());
//			changeConversationsStatus("4", id);
//			conversationInfo.setStatus(4);
//			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
//					+ moble));
//			startActivity(intent);
//		}
//		break;
//	case R.id.linear_leftnav_appointment:
//		AppointAdapter adapter1 = new AppointAdapter(context, tripsList);
//		tripsList.size();
//		adapter1.upDatas(tripsList);
//		linear_oppointment_listview.setAdapter(adapter1);
//		linear_left.setVisibility(View.GONE);
//		mMapView.setVisibility(View.GONE);
//		linear_oppointment.setVisibility(View.VISIBLE);
//		linear_oppointment_right.setVisibility(View.VISIBLE);
//		break;
//	case R.id.linear_leftnav_main:
//		linear_left.setVisibility(View.VISIBLE);
//		mMapView.setVisibility(View.VISIBLE);
//		linear_oppointment.setVisibility(View.GONE);
//		linear_oppointment_right.setVisibility(View.GONE);
//		break;
//	}
//}