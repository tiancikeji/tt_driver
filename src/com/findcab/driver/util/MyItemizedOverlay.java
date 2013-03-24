package com.findcab.driver.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	public List<OverlayItem> mGeoList = new ArrayList<OverlayItem>();

	private Context mContext;

	public MyItemizedOverlay(Context context, Drawable maker,
			List<OverlayItem> geoList) {
		super(maker);
		this.mContext = context;
		this.mGeoList = geoList;

		populate();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return mGeoList.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
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

	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		// TODO Auto-generated method stub

		return super.onTap(arg0, arg1);
	}

	boolean isShow;

	@Override
	protected boolean onTap(int index) {
		// TODO Auto-generated method stub

		if (index != 0) {

			Toast.makeText(this.mContext, mGeoList.get(index).getTitle(),
					Toast.LENGTH_SHORT).show();

		}

		return super.onTap(index);
	}
}
