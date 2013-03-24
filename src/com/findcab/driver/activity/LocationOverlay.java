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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.findcab.driver.util.MyItemizedOverlay;
import com.findcab.driver.util.Tools;

public class LocationOverlay extends Activity implements OnClickListener,
		BDLocationListener {

	protected int count = 0;
	public static MapView mMapView = null;
	// LocationListener mLocationListener = null;
	public Context context;
	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	List<GeoPoint> pList;
	int iZoom = 0;
	MapController mapController;

	private ConversationInfo conversationInfo;
	private Button answer;
	private GeoPoint pt;
	Location location;

	private double lat;
	private double lng;

	private List<PassengerInfo> listInfo;

	private RelativeLayout bottom_button;
	private DriverInfo info;
	private boolean isRun = true;
	private boolean isAnswer;// 是否应答
	private int trip_id;

	private TextView starTextView, endTextView;

	private LinearLayout layout_instruction;
	private LinearLayout line1, line2;

	private Button cancel, call, finish;
	private Button locate;
	private String androidDevice;

	private int count1;
	private int count2;
	private String id;
	private LocationClient mLocClient;
	// 存放overlayitem
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
	private String name;
	private String password;
	protected String error;

	private Button btn_call;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		initManager();
		setContentView(R.layout.mapview2);
		Tools.init();
		// initView();
		initView2();

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

	private void initView() {

		mMapView = (MapView) findViewById(R.id.bmapView);

		androidDevice = Tools.getDeviceId(context);

		iZoom = mMapView.getZoomLevel();

		answer = (Button) findViewById(R.id.answer);
		answer.setOnClickListener(this);

		bottom_button = (RelativeLayout) findViewById(R.id.bottom_button);

		starTextView = (TextView) findViewById(R.id.start);
		endTextView = (TextView) findViewById(R.id.end);

		layout_instruction = (LinearLayout) findViewById(R.id.layout_instruction);
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		call = (Button) findViewById(R.id.call);
		call.setOnClickListener(this);

		finish = (Button) findViewById(R.id.finish);
		finish.setOnClickListener(this);

		locate = (Button) findViewById(R.id.locate);
		locate.setOnClickListener(this);

		line1 = (LinearLayout) findViewById(R.id.line1);
		line2 = (LinearLayout) findViewById(R.id.line2);

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

		getData();

	}

	@Override
	protected void onPause() {

		mBMapMan.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {

		mBMapMan.start();
		isRun = true;
		super.onResume();
	}

	class MyGeneralListener implements MKGeneralListener {
		@Override
		public void onGetNetworkState(int iError) {
			Log.d("MyGeneralListener", "onGetNetworkState error is " + iError);
		}

		@Override
		public void onGetPermissionState(int iError) {
			Log.d("MyGeneralListener", "onGetPermissionState error is "
					+ iError);
			if (iError == MKEvent.ERROR_PERMISSION_DENIED) {

			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		onClick2(v);
		// switch (v.getId()) {
		// case R.id.answer: // 应答
		// id = String.valueOf(conversationInfo.getId());
		// changeConversationsStatus("1", id);
		// answer.setVisibility(View.GONE);
		// isRun = true;
		// isAnswer = true;
		// getConversations();
		// break;
		// case R.id.cancel:// 取消
		// changeConversationsStatus("2", id);
		// answer.setVisibility(View.VISIBLE);
		// layout_instruction.setVisibility(View.GONE);
		// bottom_button.setVisibility(View.GONE);
		// isRun = true;
		// isAnswer = false;
		// conversationInfo = null;
		// getConversations();
		// break;
		// case R.id.call:
		// String moble = null;
		//
		// if (listInfo != null) {
		//
		// for (int i = 0; i < listInfo.size(); i++) {
		//
		// if (listInfo.get(i).getId() == conversationInfo
		// .getFrom_id()) {
		// moble = listInfo.get(i).getMobile();
		// }
		//
		// }
		//
		// // changeConversationsStatus("4", id);
		// Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
		// + moble));
		// startActivity(intent);
		// isRun = true;
		// }
		// break;
		// case R.id.finish: // 乘客上车
		// layout_instruction.setVisibility(View.GONE);
		// bottom_button.setVisibility(View.GONE);
		// showNorm();
		// changeConversationsStatus("4", id);
		// isAnswer = false;
		// break;
		// case R.id.locate:
		// count = 0;
		// // if (initGPS()) {
		// //
		// // initLocation();
		// // }
		// break;
		// default:
		// break;
		// }
	}

	/**
	 * 获得周围的乘客
	 */
	private void getPassengers() {
		new Thread(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {

				try {

					Map<String, String> map = new HashMap<String, String>();

					if (lat != 0) {

						map.put("passenger[lat]", String.valueOf(lat));
						map.put("passenger[lng]", String.valueOf(lng));
						map.put("passenger[androidDevice]", androidDevice);
					}

					listInfo = (List<PassengerInfo>) HttpTools.getAndParse(
							Constant.DRIVERS_PASSENGERS, map,
							new PassengersHandler());

					if (listInfo != null && listInfo.size() > 0) {
						isRun = true;
						messageHandler.sendEmptyMessage(Constant.SUCCESS);
					} else {

						messageHandler.sendEmptyMessage(Constant.FAILURE);
					}
					try {
						Thread.sleep(5000);
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

		}).start();
	}

	/**
	 * 得到发布路线
	 */
	private void getTrip(final int trip_id) {
		new Thread(new Runnable() {
			public void run() {
				try {
					String result = (String) HttpTools.getAndParse(
							Constant.TRIPS + String.valueOf(trip_id), null,
							new BaseHandler());
					JSONObject object = new JSONObject(result);
					tripsInfo = new TripsInfo(object.getJSONObject("trip"));
					System.out.println("getTrip------>" + result);
					if (result != null) {
						isRun = false;// 得到一条新请求后，暂停获得新请求
						requestMessage.sendEmptyMessage(Constant.SUCCESS);
					}
				} catch (JSONException e) {
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
		new Thread(new Runnable() {

			public void run() {
				while (isRun) {

					try {

						Map<String, String> map = new HashMap<String, String>();

						map.put("to_id", String.valueOf(info.getId()));
						if (!isAnswer) {

							conversationInfo = (ConversationInfo) HttpTools
									.getAndParse(Constant.CONVERSATIONS, map,
											new ConversationsHandler(-1));

						} else {

							if (conversationInfo != null) {

								conversationInfo = (ConversationInfo) HttpTools
										.getAndParse(context,
												Constant.CONVERSATIONS, map,
												new ConversationsHandler(
														conversationInfo
																.getIndex()));
							}

						}

						if (conversationInfo != null) {

							int Status = conversationInfo.getStatus();

							System.out
									.println("-----getConversations----Status--="
											+ Status);
							if (Status == 0 && !isAnswer) {
								trip_id = conversationInfo.getTrip_id();

								getTrip(trip_id);
							} else if (Status == -1 && isAnswer) {
								giveupHandler
										.sendEmptyMessage(Constant.SUCCESS);

								count1++;
							} else if (Status == 3 && isAnswer) {
								giveupHandler
										.sendEmptyMessage(Constant.SUCCESS);
								count2++;
							}
						}

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 更新会话状态
	 */
	private void changeConversationsStatus(final String status, final String id) {
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
					}
					map.put("conversation[status]", status);
					map.put("conversation[status_desc]", status_desc);
					String result = (String) HttpTools.postAndParse(
							Constant.CONVERSATIONS + id + "/", map,
							new BaseHandler());

					if (result != null && status.equals("1")) {
						answerHandler.sendEmptyMessage(Constant.SUCCESS);
					} else if (result != null && status.equals("2")) {
						isRun = true;
						rejectHandler.sendEmptyMessage(Constant.SUCCESS);
					} else if (result != null && status.equals("3")) {
						isRun = false;
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
						String resultString = (String) HttpTools.getAndParse(
								Constant.SIGNOUT, map, new BaseHandler());
						System.out.println("signOut------->" + resultString);
						JSONObject jsonObject = new JSONObject(resultString);
						if (jsonObject.has("message")) {
							System.out.println("message--->"
									+ jsonObject.getString("message"));
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
		bottom_button.setVisibility(View.VISIBLE);
		answer.setVisibility(View.VISIBLE);
		line1.setVisibility(View.VISIBLE);
		line2.setVisibility(View.VISIBLE);
		new Plysounds(context).synthetizeInSilence("你有一条从"
				+ tripsInfo.getStart() + "到" + tripsInfo.getEnd() + "搭车请求！");

	}

	/**
	 * 显示正常状态
	 */
	private void showNorm() {
		bottom_button.setVisibility(View.GONE);
		layout_instruction.setVisibility(View.GONE);
		answer.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示应答状态
	 */
	private void showAnswer() {
		bottom_button.setBackgroundColor(Color.parseColor("#DCDCDC"));
		bottom_button.setVisibility(View.VISIBLE);
		layout_instruction.setVisibility(View.VISIBLE);
		answer.setVisibility(View.GONE);
		line1.setVisibility(View.GONE);
		line2.setVisibility(View.GONE);
		bottom_button.setBackgroundColor(Color.GRAY);

	}

	TripsInfo tripsInfo;

	List<TripsInfo> tripsList;

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				List<Overlay> list = mMapView.getOverlays();
				list.clear();
				Drawable marker = getResources().getDrawable(
						R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
				marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
						.getIntrinsicHeight());

				for (int i = 1; i < mGeoList.size(); i++) {

					mGeoList.remove(i);
				}

				int size = listInfo.size();
				OverlayItem item = null;
				int lat,
				lng;
				PassengerInfo info;
				Drawable maker = getResources().getDrawable(
						R.drawable.passenger);
				for (int i = 0; i < size; i++) {
					info = listInfo.get(i);

					lat = (int) (info.getLat() * 1e6);
					lng = (int) (info.getLng() * 1e6);
					item = new OverlayItem(new GeoPoint(lat, lng), info
							.getName(), "");
					item.setMarker(maker);

					mGeoList.add(item);
				}
				MyItemizedOverlay overlay = new MyItemizedOverlay(context,
						maker, mGeoList);

				mMapView.getOverlays().clear();
				mMapView.getOverlays().add(overlay);
				mMapView.postInvalidate();

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

				bottom_button.setVisibility(View.VISIBLE);
				starTextView.setText(tripsInfo.getStart());
				endTextView.setText(tripsInfo.getEnd());
				showNewMessage();

				break;
			case Constant.FAILURE:

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
				isRun = true;
				isAnswer = false;
				showNorm();
				if (conversationInfo.getStatus() == -1) {
					Tools.myToast(context, "乘客已取消打车");
				}
				if (conversationInfo.getStatus() == 3) {
					Tools.myToast(context, "乘客已上车");
				}
				break;
			case Constant.FAILURE:

				break;

			}
		}
	};
	private Button btn_request;
	private LinearLayout linear_called;
	private LinearLayout linear;
	private ListView listView;
	private LinearLayout linear_time;

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
					isRun = true;
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
		// TODO Auto-generated method stub
		int lat = (int) (location.getLatitude() * 1e6);
		int lng = (int) (location.getLongitude() * 1e6);
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

	}

	@Override
	public void onReceivePoi(BDLocation arg0) {
		// TODO Auto-generated method stub

	}

	private void initView2() {

		mMapView = (MapView) findViewById(R.id.bmapView);

		btn_call = (Button) findViewById(R.id.btn_call);
		btn_call.setOnClickListener(this);

		btn_request = (Button) findViewById(R.id.btn_request);
		btn_request.setOnClickListener(this);

		linear_called = (LinearLayout) findViewById(R.id.linear_called);
		linear = (LinearLayout) findViewById(R.id.linear);
		listView = (ListView) findViewById(R.id.listView);

		linear_time = (LinearLayout) findViewById(R.id.linear_time);

	}

	public void onClick2(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.btn_call:

			showCalledView();
			break;
		case R.id.btn_request:

			showPassengerListView();
			break;
		}
	}

	private void showCalledView() {

		btn_request.setVisibility(View.GONE);
		btn_call.setVisibility(View.GONE);
		linear_time.setVisibility(View.GONE);
		linear_called.setVisibility(View.VISIBLE);
		linear_called.setVisibility(View.VISIBLE);
	}

	private void showPassengerListView() {

		listView.setVisibility(View.VISIBLE);
		linear.setVisibility(View.GONE);
		btn_request.setVisibility(View.GONE);

		List<PassengerInfo> list = new ArrayList<PassengerInfo>();

		for (int i = 0; i < 3; i++) {

			list.add(new PassengerInfo());

		}
		listView.setAdapter(new InfoAdapter(context, list));
	}
}
