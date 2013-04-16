package com.findcab.driver.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.findcab.R;
import com.findcab.driver.object.TripsInfo;

public class InfoAdapter extends BaseAdapter {
//	public List<ConversationInfo> list;
	public List<TripsInfo> list;
	public int mResource = R.layout.request_item;
	public LayoutInflater mInflater;

	public Context mContext;

//	public InfoAdapter(Context context, List<ConversationInfo> list) {
//		this.list = list;
//		this.mContext = context;
//		mInflater = (LayoutInflater) context
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//	}
	public InfoAdapter(Context context, List<TripsInfo> list) {
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

		final TripsInfo info = list.get(position);
		TextView start =(TextView) convertView.findViewById(R.id.textstart1);
		TextView end =(TextView) convertView.findViewById(R.id.textend1);
		TextView money =(TextView) convertView.findViewById(R.id.textmoney1);
		TextView time =(TextView) convertView.findViewById(R.id.texttime1);
		start.setText(info.getStart());
		end.setText(info.getEnd());
		money.setText(info.getAppointment());
		time.setText(info.getUpdated_at());
//		start.setText("ggggg");
//		end.setText("ffffff");
		//money.setText(info.get);

		return convertView;
	}

	/**
	 * 更新数据
	 * 
	 * @param enterpriseInfoList
	 */
	public void upDatas(List<TripsInfo> contactsList) {

		list = contactsList;

		notifyDataSetChanged();
	}
}
