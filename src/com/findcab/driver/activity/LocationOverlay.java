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
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import cn.jpush.android.api.JPushInterface;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.findcab.R;
import com.findcab.driver.adapter.AppointAdapter;
import com.findcab.driver.adapter.InfoAdapter;
import com.findcab.driver.handler.BaseHandler;
import com.findcab.driver.handler.ConversationsHandler;
import com.findcab.driver.handler.PassengersHandler;
import com.findcab.driver.object.ConversationInfo;
import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.object.OwnerInfo;
import com.findcab.driver.object.PassengerInfo;
import com.findcab.driver.object.TripsInfo;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.MD5;
import com.findcab.driver.util.MyItemizedOverlay;
import com.findcab.driver.util.MyLogTools;
import com.findcab.driver.util.Tools;
import com.findcab.jpush.MyJpushTools;
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

	protected int count = 0;
	private int biao, lubiao = 0;
	public static MapView mMapView = null;
	// LocationListener mLocationListener = null;
	public Context context;

	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	List<GeoPoint> pList;
	TripsInfo tripsInfo = null;
	private Button btn_request, btn_refurbish, btn_road;
	private LinearLayout linear_called;
	private LinearLayout linear;
	private ListView listView;
	private LinearLayout linear_time;
	List<TripsInfo> tripsList;
	int iZoom = 0;
	MapController mapController;

	private ConversationInfo conversationInfo;
	// private Button answer;
	private GeoPoint pt;
	Location location;

	private int lat;
	private int lng;

	private List<PassengerInfo> listInfo;
	public Bundle bundle;
	private RelativeLayout bottom_button;
	private DriverInfo info;
	private OwnerInfo ownerInfo;
	// private boolean isRun = true;//轮询时使用，用来判断是否循环获取会话
	private boolean ispassengersrun = true;
	private boolean isAnswer;// 是否应答
	private int trip_id;

	private TextView starTextView, endTextView, distant, money, texttime;

	private LinearLayout layout_instruction;
	private LinearLayout linear_left;
	private LinearLayout line1, line2;

	private Button linear_call, linear_already, finish, btn_call;
	// private Button locate;
	private String androidDevice;
	List<ConversationInfo> list = new ArrayList<ConversationInfo>();
	private int count2;
	private String id;
	private LocationClient mLocClient;
	// 存放overlayitem
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	private String name;
	private String password;
	protected String error;
	private boolean isStartCount = true, isWaiting = false;// 控制是否启动倒数线程。只启动一次线程就好的
	private final int WAITING = 60;
	private int waitingTime = WAITING;

	// 我的订单控件
	private LinearLayout linearlayout_myorders;
	private ListView listview_myorders;
	// 导航栏
	private LinearLayout linear_nav;
	private Button linear_nav_main, linear_nav_oppointment, linear_nav_order,
			linear_nav_set;

	// 预约界面
	private LinearLayout linear_oppointment;
	private Button linear_oppointment_all, linear_oppointment_today,
			linear_oppointment_tommorrow, linear_oppointment_long,
			linear_oppointment_near;
	// 预约listview
	private LinearLayout linear_oppointment_right;
	private ListView linear_oppointment_listview;

	/**
	 * handler消息列表
	 */
	static final int MESSAGE_CONVERSATIONS_CHANGE = 10001;
	static final int MESSAGE_DRIVERS_CHANGE = 10002;
	static final int MESSAGE_PASSAGERS_CHANGE = 10003;

	// 处理所有LocationOverlay类中的handler异步处理
	Handler handlerMain = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CONVERSATIONS_CHANGE:
				Toast.makeText(context, "会话更新", Toast.LENGTH_SHORT).show();
				getConversations();
				break;
			case MESSAGE_DRIVERS_CHANGE:
				Toast.makeText(context, "附近司机更新", Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_PASSAGERS_CHANGE:
				Toast.makeText(context, "附近乘客更新", Toast.LENGTH_SHORT).show();
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
		// String result = HttpTools.PostDate(
		// Constant.DRIVERS_SIGNIN, map);
		//登陆
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
				MyLogTools.e("LocationOverlay-UserInfo", info.getMobile()+"-"+info.getId());
				
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
		unregisterReceiver(myreceiver);// 当主activity关闭，广播关闭
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
			// MyLogTools.d("MyGeneralListener", "onGetNetworkState error is " +
			// iError);
		}

		@Override
		public void onGetPermissionState(int iError) {
			// MyLogTools.d("MyGeneralListener",
			// "onGetPermissionState error is " + iError);
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {

			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		onClick2(v);
		switch (v.getId()) {
		case R.id.answer: // 应答
			id = String.valueOf(conversationInfo.getId());
			changeConversationsStatus("1", id);
			conversationInfo.setStatus(1);
			// answer.setVisibility(View.GONE);
			isAnswer = true;
			// getConversations();
			break;
		case R.id.cancel:// 取消
			changeConversationsStatus("2", id);
			// answer.setVisibility(View.VISIBLE);
			layout_instruction.setVisibility(View.GONE);
			bottom_button.setVisibility(View.GONE);
			isAnswer = false;
			conversationInfo = null;
			// getConversations();
			break;
		case R.id.linear_btcalled:
			String moble = null;

			if (listInfo != null) {

				for (int i = 0; i < listInfo.size(); i++) {

					if (listInfo.get(i).getId() == conversationInfo
							.getFrom_id()) {
						moble = listInfo.get(i).getMobile();
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
					Toast.makeText(context, "SIM卡没有或读取有误！", Toast.LENGTH_SHORT)
							.show();
				}
			}
			break;
		case R.id.finish: // 乘客上车
			layout_instruction.setVisibility(View.GONE);
			bottom_button.setVisibility(View.GONE);
			showNorm();
			id = String.valueOf(conversationInfo.getId());
			changeConversationsStatus("4", id);
			isAnswer = false;
			break;
		case R.id.locate:
			count = 0;
			// if (initGPS()) {
			//
			// initLocation();
			// }
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
						double lat1 = ((double) lat) / 1000000;
						double lng1 = ((double) lng) / 1000000;
						map1 = "passenger[androidDevice]=" + androidDevice
								+ "&passenger[lat]=" + String.valueOf(lat1)
								+ "&passenger[lng]=" + String.valueOf(lng1);
						
//						MyLogTools.e("获取附近乘客", lat1+":"+lng1);
						
						listInfo = (List<PassengerInfo>) HttpTools.getAndParse(
								Constant.DRIVERS_PASSENGERS, map1,
								new PassengersHandler());

						if (listInfo != null && listInfo.size() > 0) {
							// isRun = true;
							messageHandler.sendEmptyMessage(Constant.SUCCESS);
						} else {
//							MyLogTools.e("获取附近乘客失败", "无乘客数据");
							messageHandler.sendEmptyMessage(Constant.FAILURE);
						}
						try {
							//获取周围乘客一分钟刷新一次
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
	private void getTrip(final int trip_id) {
		MyLogTools.e("LocationOnverlay", "getTrip()");
		new Thread(new Runnable() {
			public void run() {

				try {
					// tripsList.clear();
					String result = (String) HttpTools.getAndParse(
							Constant.TRIPS, trip_id + "", new BaseHandler());
					JSONObject object = new JSONObject(result);
					tripsInfo = new TripsInfo(object.getJSONObject("trip"));
					// System.out.println(tripsInfo.getStart());
					tripsList.add(tripsInfo);
					// System.out.println("getTrip------>" + result);
					if (result != null) {

						requestMessage.sendEmptyMessage(Constant.SUCCESS);
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).getStatus() != 0)
							list.remove(i);

					}
					map1 = "[to_id=" + String.valueOf(info.getId()) + "]";
					if (!isAnswer) {
						MyLogTools.e("LocationOverlay-getConversation()", "1");
						list = (List<ConversationInfo>) HttpTools.getAndParse(Constant.CONVERSATIONS, map1,new ConversationsHandler());

					} else {
						MyLogTools.e("LocationOverlay-getConversation()", "2");
						if (list != null) {
							list = (List<ConversationInfo>) HttpTools
									.getAndParse(Constant.CONVERSATIONS, map1,
											new ConversationsHandler());
						}

					}

					if (list.size() > 0) {
						for (int i = 0; i < list.size(); i++) {
							conversationInfo = list.get(i);

							int Status = conversationInfo.getStatus();
							messageHandler.sendEmptyMessage(Constant.CHANGE1);

							if (Status == 0 && !isAnswer) {
								trip_id = conversationInfo.getTrip_id();

								getTrip(trip_id);
							} else if (Status == -1 && isAnswer) {
								giveupHandler
										.sendEmptyMessage(Constant.SUCCESS);

							} else if (Status == 3 && isAnswer) {
								giveupHandler
										.sendEmptyMessage(Constant.SUCCESS);
								count2++;
							}
						}
					} else {
						messageHandler.sendEmptyMessage(Constant.CHANGE);

					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				messageHandler.sendEmptyMessage(Constant.CHANGE2);
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
	 * 显示新消息
	 */
	private void showNewMessage() {
		MyLogTools.e("LocationOnverlay", "showNewMessage()");
		// bottom_button.setVisibility(View.VISIBLE);
		// answer.setVisibility(View.VISIBLE);
		// line1.setVisibility(View.VISIBLE);
		// line2.setVisibility(View.VISIBLE);
		// new Plysounds(context).synthetizeInSilence("你有一条从"
		// + tripsInfo.getStart() + "到" + tripsInfo.getEnd() + "搭车请求！");

	}

	/**
	 * 显示正常状态
	 */
	private void showNorm() {
		MyLogTools.e("LocationOnverlay", "showNorm()");
		// bottom_button.setVisibility(View.GONE);
		// layout_instruction.setVisibility(View.GONE);
		// answer.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示应答状态
	 */
	private void showAnswer() {
		MyLogTools.e("LocationOnverlay", "showAnswer()");
		// bottom_button.setBackgroundColor(Color.parseColor("#DCDCDC"));
		// bottom_button.setVisibility(View.VISIBLE);
		// layout_instruction.setVisibility(View.VISIBLE);
		// // answer.setVisibility(View.GONE);
		// line1.setVisibility(View.GONE);
		// line2.setVisibility(View.GONE);
		// bottom_button.setBackgroundColor(Color.GRAY);

	}

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constant.CHANGE1:
				MyLogTools.e("LocationOnverlay",
						"messageHandler-Constant.CHANGE1");
				// btn_request.setText(list.size()+"条新的未读打车请求");
				// // btn_request.setVisibility(View.GONE);
				// btn_call.setVisibility(View.VISIBLE);
				// starTextView.setText(tripsInfo.getStart());
				// endTextView.setText(tripsInfo.getEnd());
				// linear_time.setVisibility(View.GONE);
				break;
			case Constant.CHANGE2:
				MyLogTools.e("LocationOnverlay",
						"messageHandler-Constant.CHANGE2");
				// new Plysounds(context).synthetizeInSilence("你有一条从"
				// + tripsInfo.getStart() + "到" + tripsInfo.getEnd() + "搭车请求！");
				//

				List<Overlay> list1 = mMapView.getOverlays();
				list1.clear();
				Drawable marker = getResources().getDrawable(
						R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
				marker.setBounds(0, 0, marker.getIntrinsicWidth(),
						marker.getIntrinsicHeight());

				for (int i = 1; i < mGeoList.size(); i++) {

					mGeoList.remove(i);
				}
				//这里是打补丁，不应该在这里来处理数据不存在情况！
				if(listInfo != null && listInfo.size() != 0 && list != null&& list.size() > 0){
					int size = listInfo.size();
					OverlayItem item = null;
					int lat,
					lng;
					PassengerInfo info;
					ConversationInfo cinfo;
					Drawable maker = getResources().getDrawable(
							R.drawable.passenger);
					for (int i = 0; i < size; i++) {
						info = listInfo.get(i);

						// 获取有请求的乘客
						for (int j = 0; j < list.size(); j++) {
							cinfo = list.get(j);
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

					// OverlayTest ov = new OverlayTest(marker, context);
					// for(OverlayItem item1 : mGeoList){
					// ov.addItem(item1);
					// }
					// mMapView.getOverlays().add(ov);
					// mMapView.postInvalidate();
					// mMapView.refresh();
					// mMapView.getController().setCenter(new GeoPoint(cLat,cLon));
					MyItemizedOverlay overlay = new MyItemizedOverlay(context,
							maker, mGeoList);
					// for(OverlayItem item1 : mGeoList){
					// overlay.addItem(item1);
					// }
					// mMapView.getOverlays().clear();
					mMapView.getOverlays().add(overlay);
					mMapView.postInvalidate();
					mMapView.refresh();
					
				}else{
					//无乘客数据
					
				}
				//目前先判断路线信息是否存在，这里不晚上，以后会修改
				if(tripsInfo != null && tripsInfo.getStart() != null){
					synthetizeInSilence("你有一条从" + tripsInfo.getStart() + "到" + tripsInfo.getEnd() + "搭车请求！");
					
					linear_left.setVisibility(View.VISIBLE);
					btn_request.setText(list.size() + "条新的未读打车请求");
					// btn_request.setVisibility(View.GONE);
					if (biao == 0)
						btn_call.setVisibility(View.VISIBLE);
					// bottom_button.setVisibility(View.VISIBLE);
					starTextView.setText(tripsInfo.getStart());
					endTextView.setText(tripsInfo.getEnd());
					if (tripsInfo.getAppointment().equals(null)) {
						money.setText(0);
						distant.setText("不详");
					} else {
						money.setText(tripsInfo.getAppointment());
						distant.setText(tripsInfo.getAppointment());
					}

					// tripsList.add(tripsInfo);
					showNewMessage();
					isWaiting = true;
					// isStartCount = true;
					waitingTime = WAITING;
					countBackwards();
				}else{
					//无路线信息
					MyLogTools.e("测试路线数据是否存在", "无数据");
				}
				
				
				// isStartCount = false;
				break;

			case Constant.CHANGE:
				MyLogTools.e("LocationOnverlay",
						"messageHandler-Constant.CHANGE");
				btn_request.setText(R.string.noask);
				// btn_request.setVisibility(View.GONE);
				btn_call.setVisibility(View.GONE);
				starTextView.setText(" ");
				endTextView.setText(" ");
				money.setText(" ");
				distant.setText(" ");
				texttime.setText(" ");
				// linear_time.setVisibility(View.GONE);
				break;
			case Constant.SUCCESS:
				MyLogTools.e("LocationOnverlay",
						"messageHandler-Constant.SUCCESS");
				// Toast.makeText(context, "youchengke",
				// Toast.LENGTH_SHORT).show();
				// List<Overlay> list1 = mMapView.getOverlays();
				// list1.clear();
				// Drawable marker = getResources().getDrawable(
				// R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
				// marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				// marker.getIntrinsicHeight());
				//
				// for (int i = 1; i < mGeoList.size(); i++) {
				//
				// mGeoList.remove(i);
				// }
				//
				//
				//
				// int size = listInfo.size();
				// OverlayItem item = null;
				// int lat, lng;
				// PassengerInfo info;
				// Drawable maker = getResources().getDrawable(
				// R.drawable.passenger);
				// for (int i = 0; i < size; i++) {
				// info = listInfo.get(i);
				//
				// //获取有请求的乘客
				// if(info.getId() in )
				//
				// lat = (int) (info.getLat() * 1e6);
				// lng = (int) (info.getLng() * 1e6);
				// item = new OverlayItem(new GeoPoint(lat, lng),
				// info.getName(), "");
				//
				// item.setMarker(maker);
				//
				// mGeoList.add(item);
				// }
				// mGeoList.size();
				//
				// // OverlayTest ov = new OverlayTest(marker, context);
				// // for(OverlayItem item1 : mGeoList){
				// // ov.addItem(item1);
				// // }
				// // mMapView.getOverlays().add(ov);
				// // mMapView.postInvalidate();
				// // mMapView.refresh();
				// // mMapView.getController().setCenter(new
				// GeoPoint(cLat,cLon));
				// MyItemizedOverlay overlay = new MyItemizedOverlay(context,
				// maker, mGeoList);
				// // for(OverlayItem item1 : mGeoList){
				// // overlay.addItem(item1);
				// // }
				// // mMapView.getOverlays().clear();
				// mMapView.getOverlays().add(overlay);
				// mMapView.postInvalidate();
				// mMapView.refresh();

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
				// linear_left.setVisibility(View.VISIBLE);
				// btn_request.setText(list.size()+"条新的未读打车请求");
				// // btn_request.setVisibility(View.GONE);
				// if(biao==0)
				// btn_call.setVisibility(View.VISIBLE);
				// // bottom_button.setVisibility(View.VISIBLE);
				// starTextView.setText(tripsInfo.getStart());
				// endTextView.setText(tripsInfo.getEnd());
				// if(tripsInfo.getAppointment()==null){
				// money.setText(0);
				// distant.setText("不详");
				// }
				// else
				// {
				// money.setText(tripsInfo.getAppointment());
				// distant.setText(tripsInfo.getAppointment());
				// }
				//
				//
				// // tripsList.add(tripsInfo);
				// showNewMessage();

				break;
			case Constant.FAILURE:
				linear_left.setVisibility(View.GONE);
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
				showNorm();
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
				showNorm();
				upDate();
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};
	/**
	 * 应答
	 */
	Handler answerHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				showAnswer();
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
				showNorm();
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
		lat = (int) (location.getLatitude() * 1e6);
		lng = (int) (location.getLongitude() * 1e6);
		pt = new GeoPoint(lat, lng);
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
		androidDevice = Tools.getDeviceId(context);
		mMapView = (MapView) findViewById(R.id.bmapView);
		linear_left = (LinearLayout) findViewById(R.id.linear_left);
		btn_call = (Button) findViewById(R.id.btn_call);
		btn_call.setOnClickListener(this);

		btn_request = (Button) findViewById(R.id.btn_request);
		btn_request.setOnClickListener(this);
		btn_request.setText(R.string.noask);
		btn_refurbish = (Button) findViewById(R.id.btn_refurbish);
		btn_refurbish.setOnClickListener(this);
		btn_road = (Button) findViewById(R.id.btn_road);
		btn_road.setOnClickListener(this);
		linear_called = (LinearLayout) findViewById(R.id.linear_called);
		linear_already = (Button) findViewById(R.id.linear_already);
		linear_call = (Button) findViewById(R.id.linear_btcalled);
		linear_call.setOnClickListener(this);
		linear_already.setOnClickListener(this);
		linear = (LinearLayout) findViewById(R.id.linear);
		listView = (ListView) findViewById(R.id.listView);

		linear_time = (LinearLayout) findViewById(R.id.linear_time);
		starTextView = (TextView) findViewById(R.id.textstart);
		endTextView = (TextView) findViewById(R.id.textend);
		money = (TextView) findViewById(R.id.textmoney);
		distant = (TextView) findViewById(R.id.textdistant);
		texttime = (TextView) findViewById(R.id.texttime);
		mMapView.setDoubleClickZooming(false);
		mMapView.setClickable(false);
		mapController = mMapView.getController();
		mapController.setZoom(14);

		mLocClient = new LocationClient(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		mLocClient.setLocOption(option);
		mLocClient.registerLocationListener(this);
		mLocClient.start();
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
				btn_request.setVisibility(View.VISIBLE);
				starTextView.setText(tripsList.get(arg2).getStart());
				endTextView.setText(tripsList.get(arg2).getEnd());
				distant.setText(tripsList.get(arg2).getAppointment());
				money.setText(tripsList.get(arg2).getAppointment());
				conversationInfo = list.get(arg2);
				conversationInfo.getFrom_id();
				biao = 0;
			}

		});
		getData();

	}

	public void onClick2(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.linear_already: // 乘客上车
			// linear_left.setVisibility(View.GONE);
			// bottom_button.setVisibility(View.GONE);
			// showNorm();
			id = String.valueOf(conversationInfo.getId());
			changeConversationsStatus("4", id);
			conversationInfo.setStatus(4);
			isAnswer = false;

			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStatus() != 0)
					list.remove(i);

			}

			// isRun=true;
			// getConversations();
			biao = 0;
			btn_request.setVisibility(View.VISIBLE);
			btn_request.setText(list.size() + "条新的未读打车请求");
			btn_call.setVisibility(View.VISIBLE);
			linear_time.setVisibility(View.VISIBLE);
			linear_called.setVisibility(View.GONE);
			tripsList.clear();
			isWaiting = false;
			// isStartCount = true;
			getPassengers();
			getConversations();
			starTextView.setText(tripsInfo.getStart());
			endTextView.setText(tripsInfo.getEnd());
			money.setText(tripsInfo.getAppointment());
			distant.setText(tripsInfo.getAppointment());

			InfoAdapter adapter = new InfoAdapter(context, tripsList);
			tripsList.size();
			adapter.upDatas(tripsList);
			listView.setAdapter(adapter);

			break;
		case R.id.btn_call:
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getStatus() != 0)
					list.remove(i);

			}
			ConversationInfo conversationInfo1 = null;
			String map1 = "[to_id=" + String.valueOf(info.getId()) + "]";

			list = (List<ConversationInfo>) HttpTools.getAndParse(
					Constant.CONVERSATIONS, map1, new ConversationsHandler());

			int biaozhi = 1;
			// if (conversationInfo != null) {
			if (list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					conversationInfo1 = list.get(i);

					if (conversationInfo1.getFrom_id() == conversationInfo
							.getFrom_id()) {
						biaozhi = 0;
						if (conversationInfo1.getStatus() == 0) {
							showCalledView();
							id = String.valueOf(conversationInfo1.getId());
							changeConversationsStatus("1", id);
							conversationInfo1.setStatus(4);
							isWaiting = false;
							waitingTime = 60;
							// ispassengersrun=false;
							// isRun=false;
							// new
							// Plysounds(context).synthetizeInSilence("应答成功，请尽快联系乘客");
							synthetizeInSilence("应答成功，请尽快联系乘客");
							break;
							// ispassengersrun=true;
							// isRun=true;
							// getPassengers();
							// getConversations();
						} else if (conversationInfo1.getStatus() == -1) {
							// new
							// Plysounds(context).synthetizeInSilence("订单也被抢");
							isWaiting = false;
							synthetizeInSilence("订单已经取消");
							tripsList.clear();
							// isStartCount = true;
							getPassengers();
							getConversations();
							break;
						} else {
							isWaiting = false;
							synthetizeInSilence("订单已经被抢");
							break;
						}
						// if(conversationInfo.equals(conversationInfo1)){
						// conversationInfo1=conversationInfo;

					}

				}
				if (biaozhi == 1) {
					isWaiting = false;
					synthetizeInSilence("订单已经取消");
					// isStartCount = true;
					tripsList.clear();
					getPassengers();
					getConversations();
				}

			} else {
				isWaiting = false;
				synthetizeInSilence("订单已经取消");
				// isStartCount = true;
				tripsList.clear();
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
		case R.id.btn_request:

			showPassengerListView();
			break;
		case R.id.btn_refurbish:
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

		case R.id.linear_btcalled:
			String moble = null;

			if (listInfo != null) {

				for (int i = 0; i < listInfo.size(); i++) {

					if (listInfo.get(i).getId() == conversationInfo
							.getFrom_id()) {
						moble = listInfo.get(i).getMobile();
					}

				}
				id = String.valueOf(conversationInfo.getId());
				changeConversationsStatus("4", id);
				conversationInfo.setStatus(4);
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ moble));
				startActivity(intent);
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

	private void countBackwards() {

		// isWaiting = true;

		if (isStartCount) {
			isStartCount = false;

			new Thread(new Runnable() {
				@Override
				public void run() {
					while (isWaiting) {

						// TODO Auto-generated method stub
						try {
							Thread.currentThread().sleep(1000);
							Message msg = new Message();
							waitingTime--;
							msg.what = WAITING;
							waitingHandler.sendMessage(msg);

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					isStartCount = true;
				}

			}).start();

		}

	}

	Handler waitingHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WAITING:
				if (waitingTime >= 0) {
					texttime.setText("" + waitingTime);
					break;
				} else if (waitingTime < 0) {

					isWaiting = false;
					waitingTime = 60;
					showTimeOutDialog();
					// Intent i = new Intent(context, DialogFrist.class);
					// startActivityForResult(i, TIMEOUT);

					break;
				} else {

				}

			}

		}
	};

	/**
	 * 显示TimeOutDialog zhaochuan
	 */
	private void showTimeOutDialog() {
		synthetizeInSilence("该订单已经超时");
		tripsList.clear();
		// isStartCount = true;
		getPassengers();
		getConversations();
	}

	private void showCalledView() {
		btn_request.setVisibility(View.GONE);
		btn_call.setVisibility(View.GONE);
		biao = 1;
		linear_time.setVisibility(View.GONE);
		linear_called.setVisibility(View.VISIBLE);
		id = String.valueOf(conversationInfo.getId());
		changeConversationsStatus("1", id);
		conversationInfo.setStatus(1);
		tripsList.clear();
		getPassengers();

		getConversations();

		// InfoAdapter adapter =new InfoAdapter(context, tripsList);
		//
		// listView.setAdapter(adapter);
	}

	private void showPassengerListView() {
		if (list.size() > 0) {
			listView.setVisibility(View.VISIBLE);
			linear.setVisibility(View.GONE);
			btn_request.setVisibility(View.GONE);

			// List<ConversationInfo> list = new ArrayList<ConversationInfo>();

			// for (int i = 0; i < 3; i++) {
			//
			// list.add(new ConversationInfo());
			//
			// }
			InfoAdapter adapter = new InfoAdapter(context, tripsList);
			tripsList.size();
			adapter.upDatas(tripsList);
			listView.setAdapter(adapter);
		} else
			Tools.myToast(context, "抱歉目前没有应答");
	}

	// 此方法没有用到
	// public void getmypassengers(){
	// List<Overlay> list1 = mMapView.getOverlays();
	// list1.clear();
	// Drawable marker = getResources().getDrawable(
	// R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
	// marker.setBounds(0, 0, marker.getIntrinsicWidth(),
	// marker.getIntrinsicHeight());
	//
	// for (int i = 1; i < mGeoList.size(); i++) {
	//
	// mGeoList.remove(i);
	// }
	//
	// MyItemizedOverlay overlay = new MyItemizedOverlay(context,
	// marker, mGeoList);
	//
	// mMapView.getOverlays().add(overlay);
	// mMapView.postInvalidate();
	// mMapView.refresh();
	//
	//
	// showNewMessage();
	// }

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
		
		getApplicationContext().registerReceiver(myreceiver, filter); // 注册

	}

	/**
	 * 自定义广播,动态注册
	 */
	private BroadcastReceiver myreceiver = new BroadcastReceiver() {
		private static final String TAG = "MyReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			MyLogTools.e(TAG, "onReceive - " + intent.getAction()
					+ ", extras: " + printBundle(bundle));

			if (JPushInterface.ACTION_REGISTRATION_ID
					.equals(intent.getAction())) {
				String regId = bundle
						.getString(JPushInterface.EXTRA_REGISTRATION_ID);
				MyLogTools.e(TAG, "接收Registration Id : " + regId);
				// send the Registration Id to your server...
			} else if (JPushInterface.ACTION_UNREGISTER.equals(intent
					.getAction())) {
				String regId = bundle
						.getString(JPushInterface.EXTRA_REGISTRATION_ID);
				MyLogTools.e(TAG, "接收UnRegistration Id : " + regId);
				// send the UnRegistration Id to your server...
			} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent
					.getAction())) {
				MyLogTools
						.e(TAG,
								"接收到推送下来的自定义消息: "
										+ bundle.getString(JPushInterface.EXTRA_MESSAGE));

				// 判断收到消息内容
				refreshViewByJPushInfo(bundle
						.getString(JPushInterface.EXTRA_MESSAGE));

			} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED
					.equals(intent.getAction())) {
				MyLogTools.e(TAG, "接收到推送下来的通知");
				int notifactionId = bundle
						.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
				MyLogTools.e(TAG, "接收到推送下来的通知的ID: " + notifactionId);

			} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent
					.getAction())) {
				MyLogTools.e(TAG, "用户点击打开了通知");

				// //打开自定义的Activity
				// Intent i = new Intent(context, WelcomActivity.class);
				// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// context.startActivity(i);
				MyLogTools.e("收到广播", "");

			} else {
				MyLogTools.e(TAG, "Unhandled intent - " + intent.getAction());
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
			handlerMain.sendEmptyMessage(MESSAGE_CONVERSATIONS_CHANGE);
		} else if (text.equals("drivers")) {
			handlerMain.sendEmptyMessage(MESSAGE_DRIVERS_CHANGE);
		} else if (text.equals("passagers")) {
			handlerMain.sendEmptyMessage(MESSAGE_PASSAGERS_CHANGE);
		} else {

		}
	}
}

class OverlayTest extends ItemizedOverlay<OverlayItem> {
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	private Context mContext = null;
	static PopupOverlay pop = null;

	public OverlayTest(Drawable marker, Context context) {
		super(marker);
		this.mContext = context;
		pop = new PopupOverlay(LocationOverlay.mMapView,
				new PopupClickListener() {

					@Override
					public void onClickedPopup() {
						MyLogTools.d("hjtest  ", "clickpop");
					}
				});
		populate();

	}

	protected boolean onTap(int index) {
		Drawable marker = this.mContext.getResources().getDrawable(
				R.drawable.ic_launcher); // 得到需要标在地图上的资源
		BitmapDrawable bd = (BitmapDrawable) marker;
		Bitmap popbitmap = bd.getBitmap();
		pop.showPopup(popbitmap, mGeoList.get(index).getPoint(), 32);
		// int latspan = this.getLatSpanE6();
		// int lonspan = this.getLonSpanE6();
		Toast.makeText(this.mContext, mGeoList.get(index).getTitle(),
				Toast.LENGTH_SHORT).show();
		super.onTap(index);
		return false;
	}

	public boolean onTap(GeoPoint pt, MapView mapView) {
		if (pop != null) {
			pop.hidePop();
		}
		super.onTap(pt, mapView);
		return false;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mGeoList.get(i);
	}

	@Override
	public int size() {
		return mGeoList.size();
	}

	public void addItem(OverlayItem item) {
		mGeoList.add(item);
		populate();
	}

	public void removeItem(int index) {
		mGeoList.remove(index);
		populate();
	}

}
