package com.findcab.driver.adapter;

import java.util.List;

import com.findcab.driver.activity.R;
import com.findcab.driver.object.TripsInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

public class AppointAdapter extends BaseAdapter implements OnItemClickListener
 {
	
	private Context context;
	public List<TripsInfo> list;
	public LayoutInflater mInflater;
	public int mResource = R.layout.oppointment_item;
	
	public AppointAdapter(Context context, List<TripsInfo> list) {
		this.list = list;
		this.context = context;
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
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		if (convertView == null) {

			convertView = mInflater.inflate(mResource, null);
		
			
		}
		 TripsInfo info = list.get(position);
		TextView start =(TextView) convertView.findViewById(R.id.listview_left_from);
		TextView end =(TextView) convertView.findViewById(R.id.listview_left_to);
//		TextView money =(TextView) convertView.findViewById(R.id.textmoney1);
//		TextView time =(TextView) convertView.findViewById(R.id.texttime1);
		start.setText(info.getStart());
		end.setText(info.getEnd());
//		money.setText(info.getAppointment());
//		time.setText(info.getUpdated_at());
		return convertView;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
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
