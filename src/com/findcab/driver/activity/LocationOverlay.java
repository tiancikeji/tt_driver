package com.findcab.driver.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;
import com.findcab.driver.handler.BaseHandler;
import com.findcab.driver.handler.ConversationsHandler;
import com.findcab.driver.handler.PassengersHandler;
import com.findcab.driver.object.ConversationInfo;
import com.findcab.driver.object.DriverInfo;
import com.findcab.driver.object.PassengerInfo;
import com.findcab.driver.object.TripsInfo;
import com.findcab.driver.util.BaiLocationOverlay;
import com.findcab.driver.util.Constant;
import com.findcab.driver.util.HttpTools;
import com.findcab.driver.util.MD5;
import com.findcab.driver.util.Tools;

public class LocationOverlay extends MapActivity implements OnClickListener {

	protected int count = 0;
	static MapView mMapView = null;
	LocationListener mLocationListener = null;
	BaiLocationOverlay mLocationOverlay = null;
	public Context context;
	String mStrKey = "8BB7F0E5C9C77BD6B9B655DB928B74B6E2D838FD";
	BMapManager mBMapMan = null;
	List<GeoPoint> pList;
	static View mPopView = null; // 点击mark时弹出的气泡View
	int iZoom = 0;
	MapController mapController;
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

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mapview);
		initManager();
		Tools.init();
		mMapView = (MapView) findViewById(R.id.bmapView);

		androidDevice = Tools.getDeviceId(context);

		// 设置在缩放动画过程中也显示overlay,默认为不绘制
		mMapView.setDrawOverlayWhenZooming(true);

		// 创建点击mark时的弹出泡泡
		mPopView = super.getLayoutInflater().inflate(R.layout.popview, null);
		mMapView.addView(mPopView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
				MapView.LayoutParams.TOP_LEFT));
		mPopView.setVisibility(View.GONE);
		iZoom = mMapView.getZoomLevel();
		// mMapView.setBuiltInZoomControls(true);

		mMapView.setDrawOverlayWhenZooming(true);
		mMapView.setDoubleClickZooming(false);
		mMapView.setClickable(false);
		mapController = mMapView.getController();
		mapController.setZoom(14);
		mLocationOverlay = new BaiLocationOverlay(this, mMapView, mPopView);
		mMapView.getOverlays().add(mLocationOverlay);

		mLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {

					lat = location.getLatitude();
					lng = location.getLongitude();

					pt = new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapView.getController().animateTo(pt);
					getPassengers();
				}
			}
		};

	}

	/**
	 * 初始化BMapManager
	 */
	private void initManager() {
		context = this;
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.getLocationManager().setNotifyInternal(1000, 5000);
		mBMapMan.init(mStrKey, new MyGeneralListener());
		mBMapMan.start();
		// 如果使用地图SDK，请初始化地图Activity
		super.initMapActivity(mBMapMan);

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

		getData();
		if (!name.equals("") && !password.equals("")) {
			land(name, password);
		} else {
			Intent intent = new Intent(LocationOverlay.this, LandActivity.class);
			startActivityForResult(intent, LAND);
		}
	}

	@Override
	protected void onPause() {
		mBMapMan.getLocationManager().removeUpdates(mLocationListener);
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass();
		mBMapMan.stop();
		// isRun = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass();
		mBMapMan.start();
		isRun = true;
		super.onResume();
	}

	class OverItemT extends ItemizedOverlay<OverlayItem> {

		public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();
		private Drawable marker;
		List<PassengerInfo> infos;

		public OverItemT(Drawable marker, Context context,
				List<PassengerInfo> infos) {
			super(boundCenterBottom(marker));
			this.infos = infos;
			this.marker = marker;
			PassengerInfo info;
			GeoPoint point;
			for (int i = 0; i < infos.size(); i++) {
				info = infos.get(i);
				point = new GeoPoint((int) (info.getLat() * 1e6), (int) (info
						.getLng() * 1e6));
				mGeoList.add(new OverlayItem(point, "", ""));

				System.out.println("name--->" + info.getName());
			}

			populate();
		}

		public void updateOverlay() {
			populate();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {

			// Projection接口用于屏幕像素坐标和经纬度坐标之间的变换
			Projection projection = mapView.getProjection();
			for (int index = size() - 1; index >= 0; index--) { // 遍历mGeoList
				OverlayItem overLayItem = getItem(index); // 得到给定索引的item

				String title = overLayItem.getTitle();
				// 把经纬度变换到相对于MapView左上角的屏幕像素坐标
				Point point = projection.toPixels(overLayItem.getPoint(), null);

				// 可在此处添加您的绘制代码
				Paint paintText = new Paint();
				paintText.setColor(Color.BLUE);
				paintText.setTextSize(15);
				canvas.drawText(title, point.x - 30, point.y, paintText); // 绘制文本
			}

			super.draw(canvas, mapView, shadow);
			// 调整一个drawable边界，使得（0，0）是这个drawable底部最后一行中心的一个像素
			boundCenterBottom(marker);
		}

		@Override
		protected OverlayItem createItem(int i) {
			// TODO Auto-generated method stub
			return mGeoList.get(i);
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return mGeoList.size();
		}

		@Override
		// 处理当点击事件
		protected boolean onTap(int i) {
			setFocus(mGeoList.get(i));
			// 更新气泡位置,并使之显示
			GeoPoint pt = mGeoList.get(i).getPoint();
			LocationOverlay.mMapView.updateViewLayout(LocationOverlay.mPopView,
					new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, pt,
							MapView.LayoutParams.BOTTOM_CENTER));
			// LocationOverlay.mPopView.setVisibility(View.VISIBLE);

			Toast.makeText(context, infos.get(i).getName(), Toast.LENGTH_SHORT)
					.show();
			return true;
		}

		@Override
		public boolean onTap(GeoPoint arg0, MapView arg1) {
			// TODO Auto-generated method stub
			// 消去弹出的气泡
			LocationOverlay.mPopView.setVisibility(View.GONE);
			return super.onTap(arg0, arg1);
		}
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
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	String id;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.answer: // 应答
			id = String.valueOf(conversationInfo.getId());
			changeConversationsStatus("1", id);
			answer.setVisibility(View.GONE);
			isRun = true;
			isAnswer = true;
			getConversations();
			break;
		case R.id.cancel:// 取消
			changeConversationsStatus("2", id);
			answer.setVisibility(View.VISIBLE);
			layout_instruction.setVisibility(View.GONE);
			bottom_button.setVisibility(View.GONE);
			isRun = true;
			isAnswer = false;
			conversationInfo = null;
			getConversations();
			break;
		case R.id.call:
			String moble = null;

			if (listInfo != null) {

				for (int i = 0; i < listInfo.size(); i++) {

					if (listInfo.get(i).getId() == conversationInfo
							.getFrom_id()) {
						moble = listInfo.get(i).getMobile();
					}

				}

				// changeConversationsStatus("4", id);
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ moble));
				startActivity(intent);
				isRun = true;
			}
			break;
		case R.id.finish: // 乘客上车
			layout_instruction.setVisibility(View.GONE);
			bottom_button.setVisibility(View.GONE);
			showNorm();
			changeConversationsStatus("4", id);
			isAnswer = false;
			break;
		case R.id.locate:
			count = 0;
			if (initGPS()) {

				initLocation();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 登陆
	 */
	private void land(final String name, final String password) {
		if (HttpTools.checkNetWork(context)) {

			new Thread(new Runnable() {

				public void run() {

					try {

						// 不为空
						System.out.println("mobile------>" + name);
						System.out.println("password------>" + password);
						Map<String, String> map = new HashMap<String, String>();
						MD5 md5 = new MD5();
						map.put("driver[mobile]", name);
						map.put("driver[password]", md5.getMD5ofStr(password));

						String result = null;
						try {
							result = (String) HttpTools.postAndParse(
									Constant.DRIVERS_SIGNIN, map,
									new BaseHandler());

						} catch (Exception e) {
							// TODO: handle exception
						}

						JSONObject jsonObject;

						if (result == null) {
							return;
						}
						try {
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

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						if (info != null) {
							getConversations();

						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
						return;
					}

				}
			}).start();
		}
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
					}
					if (status.equals("2")) {

						status_desc = "reject";
					}
					if (status.equals("3")) {

						status_desc = "finish";
					}
					map.put("conversation[status]", status);
					map.put("conversation[status_desc]", status_desc);

					String result = (String) HttpTools.postAndParse(
							Constant.CONVERSATIONS + id + "/", map,
							new BaseHandler());

					if (result != null && status.equals("1")) {
						answerHandler.sendEmptyMessage(Constant.SUCCESS);
					}
					if (result != null && status.equals("2")) {
						isRun = true;
						rejectHandler.sendEmptyMessage(Constant.SUCCESS);
					}
					if (result != null && status.equals("3")) {
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

			pt = new GeoPoint((int) (location.getLatitude() * 1e6),
					(int) (location.getLongitude() * 1e6));
			mMapView.getController().animateTo(pt);
			getPassengers();
		}
	}

	ConversationInfo conversationInfo;

	/**
	 * 会话list
	 */
	// List<ConversationInfo> list;
	// List<ConversationInfo> newList;// 新会话

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

	TripsInfo tripsInfo;

	List<TripsInfo> tripsList;

	Handler messageHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Constant.SUCCESS:
				List<Overlay> list = mMapView.getOverlays();

				Drawable marker = getResources().getDrawable(
						R.drawable.iconmarka); // �õ���Ҫ���ڵ�ͼ�ϵ���Դ
				marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker
						.getIntrinsicHeight());

				OverItemT overlay = new OverItemT(marker, context, listInfo);
				mMapView.getOverlays().add(overlay);

				list.add(overlay);
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
				// new Plysounds(context).synthetizeInSilence("你有一条从"
				// + tripsInfo.getStart() + "到" + tripsInfo.getEnd()
				// + "搭车请求！");
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
	private String name;
	private String password;
	protected String error;

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
					System.out.println("-------onActivityResult------");
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

}
