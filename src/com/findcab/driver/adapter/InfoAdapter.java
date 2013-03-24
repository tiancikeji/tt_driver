package com.findcab.driver.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.findcab.driver.activity.R;
import com.findcab.driver.object.PassengerInfo;

public class InfoAdapter extends BaseAdapter {
	public List<PassengerInfo> list;
	public int mResource = R.layout.request_item;
	public LayoutInflater mInflater;

	public Context mContext;

	public InfoAdapter(Context context, List<PassengerInfo> list) {
		this.list = list;
		this.mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub

		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {

			convertView = mInflater.inflate(mResource, null);
		}

		final PassengerInfo info = list.get(position);

		return convertView;
	}

	/**
	 * 更新数据
	 * 
	 * @param enterpriseInfoList
	 */
	public void upDatas(List<PassengerInfo> contactsList) {

		list = contactsList;

		notifyDataSetChanged();
	}
}
