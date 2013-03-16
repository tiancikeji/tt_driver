package com.findcab.driver.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Projection;
import com.findcab.driver.activity.R;

public class BaiLocationOverlay extends MyLocationOverlay {
	Context context;
	private View mPopView;
	private MapView mMapView;
	private boolean isShow;

	public BaiLocationOverlay(Context arg0, MapView arg1, View view) {
		super(arg0, arg1);
		context = arg0;
		// 创建点击mark时的弹出泡泡
		mPopView = view;
		mMapView = arg1;

		// mMapView.addView(mPopView, new MapView.LayoutParams(
		// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
		// MapView.LayoutParams.BOTTOM_CENTER));
		// mPopView.setVisibility(View.GONE);

		// TODO Auto-generated constructor stub
	}

	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView,
			Location lastFix, GeoPoint myLocation, long when) {
		// TODO Auto-generated method stub
		// super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
		try {
			Projection projection = mapView.getProjection();
			Point point = new Point();
			projection.toPixels(myLocation, point);

			Resources res = context.getResources();

			Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.car);
			Bitmap bitmap1 = BitmapFactory
					.decodeResource(res, R.drawable.title);
			int x = point.x - bitmap.getWidth() / 2;
			int y = point.y - bitmap.getHeight();
			canvas.drawBitmap(bitmap, x, y, new Paint());
			// canvas.drawBitmap(bitmap1, x - bitmap1.getWidth() / 2
			// + bitmap.getWidth() / 2, y - 100, new Paint());

			mMapView.updateViewLayout(mPopView, new MapView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					myLocation, MapView.LayoutParams.BOTTOM_CENTER));
			// mPopView.setVisibility(View.VISIBLE);

		} catch (Exception e) {
			super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
		}
	}

	@Override
	public boolean onTap(GeoPoint pt, MapView arg1) {
		// TODO Auto-generated method stub

		System.out.println("-------onTap--------");

		return super.onTap(pt, arg1);
	}

}
