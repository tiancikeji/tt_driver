package com.findcab.driver.util;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.findcab.R;

public class Tools {
	public static final boolean isShow = true;

	public static void landDialog(final Context context, String content,
			String title) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(title);
		View layout = View.inflate(context, R.layout.land_dialog, null);
		TextView text_content = (TextView) layout
				.findViewById(R.id.text_content);
		if (content != null) {

			text_content.setText(content);
		}
		// aa.setCancelable(true);
		// aa.setTitle(R.string.lockscreen_charged);
		builder.setView(layout);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});

		builder.create().show();
	}

	public static void myToast(final Context context, final String text) {

		if (isShow) {

			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

		}

	}

	/**
	 * Http请求时间太长造成程序假死的情况
	 */
	public static void init() {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects()
				.penaltyLog().penaltyDeath().build());
	}

	public static String getDeviceId(Context context) {

		String android_id = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
//		System.out.println("android_id----->" + android_id);
		return android_id;
	}

	public static void onKeyDown(int keyCode, Context context) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// exitPro(context);
			exitDialog(context);
		}
	}

	public static void exitDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		// builder.setIcon(R.drawable.icon);
		// builder.setTitle("你确定要离开吗？");
		View aalayout = View.inflate(context, R.layout.exit, null);
		// aa.setCancelable(true);
		// aa.setTitle(R.string.lockscreen_charged);
		builder.setView(aalayout);
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// 这里添加点击确定后的逻辑
				// new AlertDialog.Builder(context).setMessage("你选择了确定").show();

				exitPro(context);
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// 这里添加点击确定后的逻辑
				// new AlertDialog.Builder(context).setMessage("你选择了取消").show();
			}
		});
		builder.create().show();
	}

	public static void exitPro(Context context) {

		// 杀死Application
		String packName = context.getPackageName();
		ActivityManager activityMgr = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		activityMgr.restartPackage(packName);// 需要权限<uses-permission
		// android:name="android.permission.RESTART_PACKAGES"
		// />
		activityMgr.killBackgroundProcesses(packName);// 需要权限 <uses-permission
		// android:name="android.permission.KILL_BACKGROUND_PROCESSES"/>
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
