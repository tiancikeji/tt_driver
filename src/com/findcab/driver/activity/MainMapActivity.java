package com.findcab.driver.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

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
import com.findcab.driver.activity.LocationOverlay.MyGeneralListener;
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
import com.iflytek.speech.SpeechError;
import com.iflytek.speech.SynthesizerPlayer;
import com.iflytek.speech.SynthesizerPlayerListener;

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
import android.location.Location;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainMapActivity extends Activity implements OnClickListener,BDLocationListener,SynthesizerPlayerListener{

	/**
	 * handler消息列表
	 */
	static final int MESSAGE_CONVERSATIONS_CHANGE = 10001;//推送-会话状态改变
	static final int MESSAGE_DRIVERS_CHANGE = 10002;//推送-司机信息改变
	static final int MESSAGE_PASSAGERS_CHANGE = 10003;//推送-乘客信息改变
	static final int MESSAGE_SIGNIN_FAILURE = 10004;//登陆失败
	static final int MESSAGE_SIGNIN_SUCCESS = 10005;//登陆成功
	static final int MESSAGE_GETNEARTHEPASSENGERS_SUCCESS = 10006;//获取附近乘客成功
	static final int MESSAGE_GETNEARTHEPASSENGERS_FAILURE = 10007;//获取附近乘客失败
	static final int MESSAGE_GETTRIP_SUCCESS = 10008;//获取路线成功
	static final int MESSAGE_GETTRIP_FAILURE = 10009;//获取路线失败
	static final int MESSAGE_MYCONVERSATIONS_SUCCESS = 10010;//获取会话成功
	static final int MESSAGE_MYCONVERSATIONS_FAILURE = 10011;	//获取会话失败
	static final int MESSAGE_CONVERSATION_ACCEPT_SUCCESS = 10012;//应答成功
	static final int MESSAGE_CONVERSATION_REJECT_SUCCESS = 10013;//拒绝成功
	static final int MESSAGE_CONVERSATION_FINISH_SUCCESS = 10014;//取消成功
	static final int MESSAGE_CONVERSATION_FULL_SUCCESS = 10015;//满员
	static final int MESSAGE_COUNTDOW = 10016;//倒计时


	public static final int LAND = 1;// 登陆

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

	Context context;


	//显示当前会话控件
	private ImageView imageView_startAddress;//当前会话起点图标
	private ImageView imageView_endAddress;//当前会话终点图标
	private ImageView imageView_distend;//当前会话距离图标
	private ImageView imageView_money;//当前会话加价图标
	private ImageView imageView_time;//当前会话倒计时图标
	private TextView textview_1;//元
	private TextView textview_2;//公里 
	private TextView textview_3;//秒
	private TextView textview_startAddress;//起点
	private TextView textview_endAddress;//终点
	private TextView textview_distant;//距离
	private TextView textview_money;//加价
	private TextView textview_time;//时间

	private Button button_requestList;//提示n条打车请求按钮
	private Button button_refurbish;//刷新按钮
	private Button button_road;//开启路况按钮
	private LinearLayout linear_called;
	private LinearLayout linear;
	private ListView listView;
	private LinearLayout linear_time;

	private LinearLayout layout_instruction;
	private LinearLayout linear_left;
	private LinearLayout line1, line2;

	private Button linear_call;//拨打乘客电话
	private Button linear_already;//乘客以上车
	private Button btn_call;//应答
	// 我的订单控件
	private LinearLayout linearlayout_myorders;
	private ListView listview_myorders;
	// 导航栏
	private LinearLayout linear_nav;
	private Button linear_nav_main;
	private Button linear_nav_oppointment;
	private Button linear_nav_order;
	private Button linear_nav_set;
	// 预约界面
	private LinearLayout linear_oppointment;
	private Button linear_oppointment_all;
	private Button linear_oppointment_today;
	private Button linear_oppointment_tommorrow;
	private Button linear_oppointment_long;
	private Button linear_oppointment_near;
	// 预约listview
	private LinearLayout linear_oppointment_right;
	private ListView linear_oppointment_listview;
	//地图控件
	public static MapView mMapView = null;

	//地图
	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	List<GeoPoint> pList;
	MapController mapController;

	public Bundle bundle;//用来获取用户信息

	private String name;//用户名
	private String password;//密码

	private DriverInfo ownerInfo;//用户信息
	private List<ConversationInfo> listConversationInfo = new ArrayList<ConversationInfo>();//用户所有会话
	private List<PassengerInfo> listPassengerInfo;//保存附近乘客信息
	private ConversationInfo nowConversationInfo;//当前显示回话
	private List<TripsInfo> tripsList;//路线列表
//	private TripsInfo nowTripsInfo = null;//当前路线信息

	private int lat;
	private int lng;

	// 定位用户坐标
	private double latOnwer;
	private double lngOnwer;
	// 存放overlayitem
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	//定位
	private LocationClient mLocClient;

	//电话控制
	TelephonyManager manager ; 

	private String androidDevice;//手机卡唯一标示


	//布尔值
	boolean isGetNearThePassengers = true;//判断是否刷新附近乘客信息
	boolean isShowRoad = false;
	boolean isAnswer = false;

	//倒计时参数
	private final int WAITING = 90;
	private int waitingTime = WAITING;
	private boolean isWaiting = false;
	private boolean isStartCountDown = true;

	/**
	 * 处理所有异步操作
	 */
	Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CONVERSATIONS_CHANGE:
				Toast.makeText(context, "会话更新", Toast.LENGTH_SHORT).show();
				Log.e("刷新会话", "会话更新");
				getConversations();
				break;
			case MESSAGE_DRIVERS_CHANGE:
				Toast.makeText(context, "附近司机更新", Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_PASSAGERS_CHANGE:
				Toast.makeText(context, "附近乘客更新", Toast.LENGTH_SHORT).show();
				getNearThePassengers();
				break;
			case MESSAGE_GETNEARTHEPASSENGERS_SUCCESS:
				//获取我的会话成功,将乘客坐标点画入地图
				setPassengerGeoPointsView();
				//显示最新一条会话
				showNowConversationView();

				break;
			case MESSAGE_GETNEARTHEPASSENGERS_FAILURE:
				Tools.myToast(context, "附近没有乘客哦");
				break;
			case MESSAGE_GETTRIP_SUCCESS:
				//获取路线呢成功

				break;
			case MESSAGE_GETTRIP_FAILURE:

				break;
			case MESSAGE_MYCONVERSATIONS_SUCCESS:
				//判断获取新会话状态与当前会话不同

				if(nowConversationInfo != null){
					ConversationInfo temp = null;
					for(int i = 0; i < listConversationInfo.size() ;i++){
						temp = listConversationInfo.get(i);
						if(nowConversationInfo.getId() == temp.getId()){
							//判断会话状态
							if(temp.getStatus() == 0){//新会话

							}else if(temp.getStatus() == 1){//已应答
								nowConversationInfo.setStatus(1);
								showNowConversationView();
							}else if(temp.getStatus() == 2){//拒绝

							}else if(temp.getStatus() == 3){//

							}else if(temp.getStatus() == 4){//上车

							}else if(temp.getStatus() == -1){//取消，过期
								if(listConversationInfo.size()>1){
									nowConversationInfo = listConversationInfo.get(listConversationInfo.size()-1);
								}else{
									nowConversationInfo = null;
								}
								showNowConversationView();
							}
						}
					}
				}else{
					//如果之前没有回话，则取最后一条
					nowConversationInfo = listConversationInfo.get(listConversationInfo.size()-1);
//					//取当前会话对应的路线
//					for(int i=0 ; i < tripsList.size();i++){
//						if(tripsList.get(i).getId() == nowConversationInfo.getId()){
//							nowTripsInfo = tripsList.get(i);
//						}
//					}
					showNowConversationView();
				}

				break;
			case MESSAGE_MYCONVERSATIONS_FAILURE:
				//有两种情况：无数据和失败
				break;
			case MESSAGE_CONVERSATION_ACCEPT_SUCCESS:
				//应答成功，显示以应答页面
				showCalledView();
				break;
			case MESSAGE_CONVERSATION_REJECT_SUCCESS:
				//拒绝，显示新会话
				showNowConversationView();
				break;
			case MESSAGE_CONVERSATION_FINISH_SUCCESS:
				//打车完成
				showNowConversationView();
				upDate();
				break;
			case MESSAGE_CONVERSATION_FULL_SUCCESS:
				//满员
				showNowConversationView();
				break;
			case MESSAGE_COUNTDOW:
				//倒计时
				if(waitingTime >= 0){
					textview_time.setText("" + waitingTime);
					break;
				}else if(waitingTime <0){
					synthetizeInSilence("该订单已经超时");
					//小于0，倒计时完成
					isWaiting = false;
					waitingTime = WAITING;
					//删除当前会话
					for(int i = 0; i< tripsList.size();i++){
						if(nowConversationInfo.getId() == tripsList.get(i).getId()){
							tripsList.remove(i);
						}
					}
					for(int i = 0; i< listConversationInfo.size();i++){
						if(nowConversationInfo.getId() == listConversationInfo.get(i).getId()){
							listConversationInfo.remove(i);
						}
					}
					//重置nowConversationInfo,显示最新会话
					if(listConversationInfo.size()>0){
						nowConversationInfo = listConversationInfo.get(listConversationInfo.size()-1);
//						for(int i = 0; i< tripsList.size();i++){
//							if(nowConversationInfo.getId() == tripsList.get(i).getId()){
//								nowTripsInfo = tripsList.get(i);
//							}
//						}
					}else{
						getConversations();
					}
				}
				break;
			}
		}

	};


	@Override
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

		initManager();// 初始化地图
		setContentView(R.layout.mapview2);
		Tools.init();// 检查网络，长时间请求问题

		initView();//初始化控件
		mSharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);

		//语音播报提示框
		mToast = Toast.makeText(context,
				String.format(getString(R.string.tts_toast_format), 0, 0),
				Toast.LENGTH_LONG);
		androidDevice = Tools.getDeviceId(context);//获取手机卡device_id

		initPhone();

		getData();//从sharepreferences中读取用户数据

		startLocation();

		//登陆
		signin();

		//第一次进入获取附近乘客
		getNearThePassengers();
		//获取我的会话
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
					ownerInfo = (DriverInfo) bundle.getSerializable("DriverInfo");
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitDialog(context);
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 初始化控件
	 */
	private void initView(){
		mMapView = (MapView) findViewById(R.id.bmapView);
		linear_left = (LinearLayout) findViewById(R.id.linear_left);
		btn_call = (Button) findViewById(R.id.btn_call);
		btn_call.setOnClickListener(this);

		button_requestList = (Button) findViewById(R.id.btn_request);
		button_requestList.setOnClickListener(this);
		button_requestList.setText(R.string.noask);
		button_refurbish = (Button) findViewById(R.id.btn_refurbish);
		button_refurbish.setOnClickListener(this);
		button_road = (Button) findViewById(R.id.btn_road);
		button_road.setOnClickListener(this);

		linear_called = (LinearLayout) findViewById(R.id.linear_called);
		linear_already = (Button) findViewById(R.id.linear_already);
		linear_call = (Button) findViewById(R.id.linear_btcalled);
		linear_call.setOnClickListener(this);
		linear_already.setOnClickListener(this);
		linear = (LinearLayout) findViewById(R.id.linear);
		listView = (ListView) findViewById(R.id.listView);

		//显示当前会话控件
		imageView_startAddress=(ImageView)findViewById(R.id.imageViewstart);
		imageView_endAddress=(ImageView)findViewById(R.id.imageViewend);
		imageView_distend=(ImageView)findViewById(R.id.imageViewdistend);
		imageView_money=(ImageView)findViewById(R.id.imageViewmoney);
		imageView_money.setVisibility(View.INVISIBLE);
		imageView_time=(ImageView)findViewById(R.id.imageViewtime);
		textview_1=(TextView)findViewById(R.id.textViewwenzi1);
		textview_1.setVisibility(View.INVISIBLE);
		textview_2=(TextView)findViewById(R.id.textViewwenzi2);
		textview_3=(TextView)findViewById(R.id.textViewwenzi3);
		linear_time = (LinearLayout) findViewById(R.id.linear_time);
		textview_startAddress = (TextView) findViewById(R.id.textstart);
		textview_endAddress = (TextView) findViewById(R.id.textend);
		textview_money = (TextView) findViewById(R.id.textmoney);
		textview_money.setVisibility(View.INVISIBLE);
		textview_distant = (TextView) findViewById(R.id.textdistant);
		textview_time = (TextView) findViewById(R.id.texttime);

		//地图
		mMapView.setDoubleClickZooming(false);
		mMapView.setClickable(false);
		mapController = mMapView.getController();
		mapController.setZoom(14);

		btn_call.setVisibility(View.GONE);
		// linear_left.setVisibility(View.GONE);
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
		// ,linear_nav_oppointment,linear_nav_order,linear_nav_set;

		// 我的订单控件初始化
		linearlayout_myorders = (LinearLayout) findViewById(R.id.mapview2_linearlayout_myorders);
		listview_myorders = (ListView) linearlayout_myorders
		.findViewById(R.id.my_orders_listview);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int tem = arg2;
				listView.setVisibility(View.GONE);
				linear.setVisibility(View.VISIBLE);
				button_requestList.setVisibility(View.VISIBLE);
//				textview_startAddress.setText(tripsList.get(arg2).getStart());
//				textview_endAddress.setText(tripsList.get(arg2).getEnd());
//				textview_distant.setText(tripsList.get(arg2).getAppointment());
//				textview_money.setText(tripsList.get(arg2).getAppointment());
				
				textview_startAddress.setText(nowConversationInfo.getTripInfo().getStart());
				textview_endAddress.setText(nowConversationInfo.getTripInfo().getEnd());
				textview_distant.setText(nowConversationInfo.getTripInfo().getAppointment());
				textview_money.setText(nowConversationInfo.getTripInfo().getAppointment());
				
				nowConversationInfo = listConversationInfo.get(arg2);
				nowConversationInfo.getFrom_id();
				//语音播报，未写
				isAnswer = false;
			}

		});
	}

	/**
	 * 显示会话列表
	 */
	private void showPassengerListView() {
		if (listConversationInfo.size() > 0) {
			listView.setVisibility(View.VISIBLE);
			linear.setVisibility(View.GONE);
			button_requestList.setVisibility(View.GONE);

			InfoAdapter adapter = new InfoAdapter(context, tripsList);
			tripsList.size();
			adapter.upDatas(tripsList);
			listView.setAdapter(adapter);
		} else
			Tools.myToast(context, "抱歉目前没有应答");
	}

	/**
	 * 显示应答后页面
	 */
	private void showCalledView() {//showAnswer()同一个方法
		show();
		button_requestList.setVisibility(View.GONE);
		btn_call.setVisibility(View.GONE);

		isAnswer = true;
		linear_time.setVisibility(View.GONE);
		linear_called.setVisibility(View.VISIBLE);

		//李吉喆加入，显示订单信息
		for(int i = 0; i < tripsList.size();i++){
			if(tripsList.get(i).getId() == nowConversationInfo.getId()){
				textview_startAddress.setText(nowConversationInfo.getTripInfo().getStart());
				textview_endAddress.setText(nowConversationInfo.getTripInfo().getEnd());
				if (nowConversationInfo.getTripInfo().getAppointment().equals(null)) {
					textview_money.setText(0);
					textview_distant.setText("不详");
				} else {
					textview_money.setText(nowConversationInfo.getTripInfo().getAppointment());
					textview_distant.setText(nowConversationInfo.getTripInfo().getAppointment());
				}
			}
		}
		tripsList.clear();
		getNearThePassengers();
		getConversations();

	}

	/**
	 * 显示最新会话
	 */
	private void showNowConversationView(){//同showNorm()方法
		MyLogTools.e("MainMapActivity", "showNowConversationView()");
		//判断数据是否存在
		if(nowConversationInfo.getTripInfo() != null && nowConversationInfo.getTripInfo().getStart() != null){
			show();
			synthetizeInSilence("你有一条从" + nowConversationInfo.getTripInfo().getStart() + "到"
					+ nowConversationInfo.getTripInfo().getEnd() + "搭车请求！");
			linear_left.setVisibility(View.VISIBLE);
			button_requestList.setText(listConversationInfo.size() + "条新的未读打车请求");
			if(isAnswer){
				btn_call.setVisibility(View.VISIBLE);
			}

			textview_startAddress.setText(nowConversationInfo.getTripInfo().getStart());
			textview_endAddress.setText(nowConversationInfo.getTripInfo().getEnd());
			if (nowConversationInfo.getTripInfo().getAppointment().equals(null)) {
				textview_money.setText(0);
				textview_distant.setText("不详");
			} else {
				textview_money.setText(nowConversationInfo.getTripInfo().getAppointment());
				textview_distant.setText(nowConversationInfo.getTripInfo().getAppointment());
			}

			//加入装填，判断是否在等待应答

		}else{
			//如果无会话数据则隐藏控件
//			hide();
		}
	}

	/**
	 * 隐藏当前会话控件
	 */
	private void hide()
	{
		imageView_startAddress.setVisibility(View.GONE);
		imageView_endAddress.setVisibility(View.GONE);
		imageView_distend.setVisibility(View.GONE);
		imageView_money.setVisibility(View.GONE);
		imageView_time.setVisibility(View.GONE);
		textview_1.setVisibility(View.GONE);
		textview_2.setVisibility(View.GONE);
		textview_3.setVisibility(View.GONE);
	}

	/**
	 * 显示当前会话控件
	 */
	private void show()
	{
		imageView_startAddress.setVisibility(View.VISIBLE);
		imageView_endAddress.setVisibility(View.VISIBLE);
		imageView_distend.setVisibility(View.VISIBLE);
		//		imageView_money.setVisibility(View.VISIBLE);//此版本不显示加价
		imageView_time.setVisibility(View.VISIBLE);
//		textview_1.setVisibility(View.VISIBLE);
		textview_2.setVisibility(View.VISIBLE);
		textview_3.setVisibility(View.VISIBLE);
	}

	/**
	 * 弹出退出提示框
	 */
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

				if (ownerInfo != null) {
					signOut();
				} else {
					exitPro(context);
				}
			}
		});

		builder.create().show();
	}

	/**
	 * 在地图上标注乘客点
	 */
	private void setPassengerGeoPointsView(){
		MyLogTools.e("MainMapActivity", "setPassengerGeoPointsView");
		List<Overlay> temp_overlayList = mMapView.getOverlays();
		temp_overlayList.clear();
		Drawable marker = getResources().getDrawable(
				R.drawable.iconmarka); 
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		for (int i = 1; i < mGeoList.size(); i++) {
			mGeoList.remove(i);
		}
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
		}else {
			// 无乘客数据
			MyLogTools.e("测试乘客数据是否存在", "无乘客数据");
		}

	}

	/**
	 * 初始化打电话
	 */
	private void initPhone(){
		//打电话
		manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE); 
		manager.listen(new PhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE); 


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

	/**
	 * 登陆
	 */
	private void signin(){
		// 登陆
		Map<String, String> map = new HashMap<String, String>();
		MD5 md5 = new MD5();
		map.put("driver[mobile]", name);
		map.put("driver[password]", md5.getMD5ofStr(password));

		String result = (String) HttpTools.postAndParse(
				Constant.DRIVERS_SIGNIN, map, new BaseHandler());

		// 这里在获取什么数据
		JSONObject jsonObject;
		try {

			if (result != null) {
				jsonObject = new JSONObject(result);
				if (jsonObject.has("error")) {

					String error = jsonObject.getString("error");
					messageHandler.sendEmptyMessage(MESSAGE_SIGNIN_FAILURE);
					return;
				}

				JSONObject object = jsonObject.getJSONObject("driver");
				ownerInfo = new DriverInfo(object);
				MyLogTools.e("LocationOverlay-UserInfo", ownerInfo.getMobile() + "-"
						+ ownerInfo.getId());

				// ownerInfo = new
				// OwnerInfo(object);//以后都用这个类，DriverInfo可以用来保存所有司机信息
				upDatedes();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 开始定位
	 */
	private void startLocation(){
		mLocClient = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		mLocClient.setLocOption(option);
		mLocClient.registerLocationListener(this);
		mLocClient.start();
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
					if (ownerInfo != null) {
						result = (String) HttpTools.postAndParse(
								Constant.UPDATE + String.valueOf(ownerInfo.getId()),
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
	 * 获取附近乘客
	 */
	private void getNearThePassengers(){
		MyLogTools.e("MainMapActivity", "getNearThePassengers()");
		new Thread(new Runnable(){

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				//循环获取附近乘客，5分钟获取一次
				while(isGetNearThePassengers){
					String temp_map = null;
					temp_map = "passenger[androidDevice]=" + androidDevice
					+ "&passenger[lat]=" + String.valueOf(latOnwer)
					+ "&passenger[lng]=" + String.valueOf(lngOnwer);

					listPassengerInfo = (List<PassengerInfo>) HttpTools.getAndParse(
							Constant.DRIVERS_PASSENGERS, temp_map,
							new PassengersHandler());

					if (listPassengerInfo != null && listPassengerInfo.size() > 0) {
						// 这里应该刷新地图
						messageHandler.sendEmptyMessage(MESSAGE_GETNEARTHEPASSENGERS_SUCCESS);//此消息没做处理

					} else {
						// MyLogTools.e("获取附近乘客失败", "无乘客数据");
						messageHandler.sendEmptyMessage(MESSAGE_GETNEARTHEPASSENGERS_FAILURE);
					}

					try {
						// 获取周围乘客一分钟刷新一次
						Thread.sleep(300000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}).start();
	}

	/**
	 * 获取路线信息路线
	 * @param trip_id
	 */
	private void getTrip(final ConversationInfo info,final int trip_id){
		MyLogTools.e("MainMapActivity", "getTrip()");
		new Thread(new Runnable() {
			public void run() {

				try {
					String result = (String) HttpTools.getAndParse(
							Constant.TRIPS, trip_id + "", new BaseHandler());
					JSONObject object = new JSONObject(result);

					TripsInfo temp_tripsInfo = new TripsInfo(object.getJSONObject("trip"));
					info.setTripsInfo(temp_tripsInfo);
					tripsList.add(temp_tripsInfo);

					//得到发布路线，这里处理的不对
					if (result != null) {
						messageHandler.sendEmptyMessage(MESSAGE_GETTRIP_SUCCESS);
					}else{
						messageHandler.sendEmptyMessage(MESSAGE_GETTRIP_FAILURE);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

			}

		}).start();
	}

	/**
	 * 获取我的会话
	 */
	private void getConversations(){
		MyLogTools.e("MainMapActivity", "getConversations()");
		new Thread(new Runnable(){

			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				String temp_map = null;
				temp_map =  "[to_id=" + String.valueOf(ownerInfo.getId()) + "]";//通过用户id获取会话

				for (int i = 0; i < listConversationInfo.size(); i++) {
					if (listConversationInfo.get(i).getStatus() != 0)
						listConversationInfo.remove(i);

				}
				
				//这里始终获取最新会话，当获取成功后判断是否当前会话状态被改变（还需要判断会话过期）
				listConversationInfo = (List<ConversationInfo>) HttpTools.getAndParse(
						Constant.CONVERSATIONS, temp_map,
						new ConversationsHandler());

				if(listConversationInfo.size() > 0){
					ConversationInfo temp_conversationInfo = null;
					int temp_status;
					int temp_tripId;
					for(int i = 0; i < listConversationInfo.size();i++){
						temp_conversationInfo = listConversationInfo.get(i);
						temp_status = temp_conversationInfo.getStatus();

						//先保存所有获取数据，再在使用中自行获取需要数据
						if (temp_status == 0 && !isAnswer) {
							temp_tripId = temp_conversationInfo.getTrip_id();
							//获取会话的路线信息
							getTrip(listConversationInfo.get(i),temp_tripId);
						}
						//						//判断会话状态
						//						if (temp_status == 0 && !isAnswer) {
						//							temp_tripId = temp_conversationInfo.getTrip_id();
						//							//获取会话的路线信息
						//							getTrip(temp_tripId);
						//						} else if (temp_status == -1 && isAnswer) {
						//							//-1为用户取消
						//							messageHandler.sendEmptyMessage(MESSAGE_CONVERSATION_FINISH_SUCCESS);
						//						} else if (temp_status == 3 && isAnswer) {
						//							//3为用户取消，放弃
						//							messageHandler.sendEmptyMessage(MESSAGE_CONVERSATION_FINISH_SUCCESS);
						//						}
					}
					messageHandler.sendEmptyMessage(MESSAGE_MYCONVERSATIONS_SUCCESS);
				}else{
					messageHandler.sendEmptyMessage(MESSAGE_MYCONVERSATIONS_FAILURE);

				}
			}

		}).start();
	}

	/**
	 * 更新会话状态
	 */
	private void changeConversationsStatus(final String status, final String id) {
		MyLogTools.e("LocationOnverlay", "changeConversationsStatus()");
		new Thread(new Runnable() {
			public void run() {
				try {

					Map<String, String> temp_map = new HashMap<String, String>();

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
					temp_map.put("conversation[status]", status);
					temp_map.put("conversation[status_desc]", status_desc);

					String result = (String) HttpTools.postAndParse(
							Constant.CONVERSATIONS1 + id + "/", temp_map,
							new BaseHandler());
					//不同的更改状态，对应不同的操作
					if (result != null && status.equals("1")) {
						messageHandler.sendEmptyMessage(MESSAGE_CONVERSATION_ACCEPT_SUCCESS);
					} else if (result != null && status.equals("2")) {
						messageHandler.sendEmptyMessage(MESSAGE_CONVERSATION_REJECT_SUCCESS);
					} else if (result != null && status.equals("3")) {//满员
						messageHandler.sendEmptyMessage(MESSAGE_CONVERSATION_FULL_SUCCESS);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return;
				}

			}
		}).start();
	}

	//目前没处理次状态
	/**
	 * 更新客满状态
	 */
	private void upDate() {
		MyLogTools.e("MainMapActivity", "upDate()");
		new Thread(new Runnable() {
			public void run() {
				try {

					Map<String, String> temp_map = new HashMap<String, String>();

					if (lat != 0) {
						temp_map.put("driver[status]", "1");
					}
					String result = null;
					if (ownerInfo != null) {
						result = (String) HttpTools.postAndParse(
								Constant.UPDATE + String.valueOf(ownerInfo.getId()),
								temp_map, new BaseHandler());
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

	/**
	 * 倒计时
	 */
	private void countBackwards(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				while(isWaiting){
					try {
						Thread.currentThread().sleep(1000);
						waitingTime--;
						messageHandler.sendEmptyMessage(MESSAGE_COUNTDOW);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
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
						JSONObject jsonObject = new JSONObject(resultString);
						if (jsonObject.has("message")) {

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

	/**
	 * 退出，杀死所有页面
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

		mSynthesizerPlayer.setVoiceName("vixy");

		// 设置发音人语速
		int speed = mSharedPreferences.getInt(
				context.getString(R.string.preference_key_tts_speed), 50);
		mSynthesizerPlayer.setSpeed(speed);

		mSynthesizerPlayer.setVolume(99);

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
	 * 语音播报
	 */
	@Override
	public void onBufferPercent(int percent, int arg1, int arg2) {

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

	@Override
	public void onReceiveLocation(BDLocation location) {
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

		getNearThePassengers();

		getConversations();

	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.linear_btcalled:{
			String moble = null;

			if (listPassengerInfo != null) {

				for (int i = 0; i < listPassengerInfo.size(); i++) {

					if (listPassengerInfo.get(i).getId() == nowConversationInfo
							.getFrom_id()) {
						moble = listPassengerInfo.get(i).getMobile();
					}

				}
				changeConversationsStatus("4", String.valueOf(nowConversationInfo.getId()));

				TelephonyManager tm = (TelephonyManager) getBaseContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
					Intent intent = new Intent(Intent.ACTION_CALL,
							Uri.parse("tel:" + moble));

					startActivity(intent);

				} else {
					Toast.makeText(context, "SIM卡没有或读取有误！", Toast.LENGTH_SHORT)
					.show();
				}
			}
		}
		break;
		case R.id.linear_already: {
			// 乘客以上车
			
			String temp_id = String.valueOf(nowConversationInfo.getId());
			changeConversationsStatus("4", temp_id);//4代表乘客以上车
			nowConversationInfo.setStatus(4);
			nowConversationInfo = null;
			
			isAnswer = false;
			
			for (int i = 0; i < listConversationInfo.size(); i++) {
				if (listConversationInfo.get(i).getStatus() != 0)
					listConversationInfo.remove(i);
			}
			
			//删除已关闭会话，当前会话重新赋值
			if(listConversationInfo.size()>0){
				nowConversationInfo = listConversationInfo.get(listConversationInfo.size()-1);
			}
//			for(int i = 0; i< tripsList.size();i++){
//				if(tripsList.get(i).getId() == nowConversationInfo.getId()){
//					nowTripsInfo = tripsList.get(i);
//				}
//			}
			
			button_requestList.setVisibility(View.VISIBLE);
			button_requestList.setText(listConversationInfo.size() + "条新的未读打车请求");
			btn_call.setVisibility(View.VISIBLE);
			linear_time.setVisibility(View.VISIBLE);
			linear_called.setVisibility(View.GONE);
			tripsList.clear();
			isWaiting = false;
			
			getConversations();
			
			showNowConversationView();
			
			InfoAdapter adapter = new InfoAdapter(context, tripsList);
			tripsList.size();
			adapter.upDatas(tripsList);
			listView.setAdapter(adapter);
		}
		break;
		case R.id.btn_call:{
			//应答
			for (int i = 0; i < listConversationInfo.size(); i++) {
				if (listConversationInfo.get(i).getStatus() != 0)
					listConversationInfo.remove(i);

			}
			ConversationInfo conversationInfo1 = null;
			String temp_map = "[to_id=" + String.valueOf(ownerInfo.getId()) + "]";

			listConversationInfo = (List<ConversationInfo>) HttpTools.getAndParse(
					Constant.CONVERSATIONS, temp_map, new ConversationsHandler());

			int biaozhi = 1;
			if (listConversationInfo.size() > 0) {
				for (int i = 0; i < listConversationInfo.size(); i++) {
					conversationInfo1 = listConversationInfo.get(i);

					if (conversationInfo1.getFrom_id() == nowConversationInfo.getFrom_id()) {
						biaozhi = 0;
						if (conversationInfo1.getStatus() == 0) {
							
							changeConversationsStatus("1", String.valueOf(conversationInfo1.getId()));
							conversationInfo1.setStatus(1);
							nowConversationInfo=conversationInfo1;
							
//							getTrip(conversationInfo1.getId());//当应答后，从新获取路线信息
							
							showCalledView();
							
							isWaiting = false;
							waitingTime = 60;
							// ispassengersrun=false;
							// isRun=false;
							synthetizeInSilence("应答成功，请尽快联系乘客");
//							isMyconversationRun=true;
							break;
						} else if (conversationInfo1.getStatus() == -1) {
							isWaiting = false;
							synthetizeInSilence("订单已经取消");
							tripsList.clear();
							nowConversationInfo = null;
							getNearThePassengers();
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
					// isStartCount = true;
					tripsList.clear();
//					countbiao=0;
					getNearThePassengers();
					getConversations();
				}

			} else {
				isWaiting = false;
				synthetizeInSilence("订单已经取消");
				// isStartCount = true;
				tripsList.clear();
//				countbiao=0;
				getNearThePassengers();
				getConversations();
			}
			
		}
		break;
		case R.id.btn_request:
			showPassengerListView();
			break;
		case R.id.btn_refurbish:{
			tripsList.clear();
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
			getNearThePassengers();
			getConversations();
			mapController.animateTo(new GeoPoint(lat, lng), null);
			mMapView.refresh();
		}
		break;
		case R.id.btn_road:{
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
			getNearThePassengers();
			getConversations();
			mapController.animateTo(new GeoPoint(lat, lng), null);
			mMapView.refresh();
			setPassengerGeoPointsView();
			if (!isShowRoad) {
				isShowRoad = true;
				mMapView.setTraffic(true);
				mMapView.refresh();
			} else {
				isShowRoad = false;
				mMapView.setTraffic(false);
				mMapView.refresh();

			}
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
		}

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

	};

	// 通过推送消息内容判断刷新内容，并通知handler操作;1,conversation_change 2,drivers_change
	// 3,passagers_change
	private void refreshViewByJPushInfo(String text) {
		if (text.equals("conversations")) {
			Log.e("收到推送", "conversations");
			messageHandler.sendEmptyMessage(MESSAGE_CONVERSATIONS_CHANGE);
		} else if (text.equals("drivers")) {
			messageHandler.sendEmptyMessage(MESSAGE_DRIVERS_CHANGE);
		} else if (text.equals("passagers")) {
			messageHandler.sendEmptyMessage(MESSAGE_PASSAGERS_CHANGE);
		} else {

		}
	}

	/**
	 * 电话监听
	 * @author li
	 *
	 */
	private class PhoneCallListener extends PhoneStateListener {
		private boolean bphonecalling = false;

		@Override
		public void onCallStateChanged(int state, String incomingnumber) {
			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				bphonecalling = true;
			} else if (TelephonyManager.CALL_STATE_IDLE == state
					&& bphonecalling) {
				bphonecalling = false; 

				//打电话完成返回本页面
				Intent intent=new Intent(); 
				intent.setClass(MainMapActivity.this,MainMapActivity.class);  
				startActivity(intent);  

				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
			super.onCallStateChanged(state, incomingnumber);
		}
	}

	/**
	 * 地图监听？没弄懂作用
	 * @author li
	 *
	 */
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

}
