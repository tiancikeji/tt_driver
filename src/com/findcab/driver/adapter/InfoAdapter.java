package com.findcab.driver.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.findcab.R;
import com.findcab.driver.object.ConversationInfo;
import com.findcab.driver.object.TripsInfo;
import com.findcab.driver.util.MyLogTools;

public class InfoAdapter extends BaseAdapter {
	static final int MESSAGE_COUNTDOWN = 10001;//倒计时
	
	public List<ConversationInfo> listConversationInfo;
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

	public void setConversationsInfo(List<ConversationInfo> listConversationInfo){
		this.listConversationInfo = listConversationInfo;
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
		if (convertView == null) {
			convertView = mInflater.inflate(mResource, null);
		}

		final TripsInfo info = list.get(position);
		TextView start =(TextView) convertView.findViewById(R.id.textview_start);
		TextView end =(TextView) convertView.findViewById(R.id.textview_end);
		TextView money =(TextView) convertView.findViewById(R.id.textview_money);
		final TextView time =(TextView) convertView.findViewById(R.id.textview_time);
		start.setText(info.getStart());
		end.setText(info.getEnd());
		
		money.setText(listConversationInfo.get(position).getDistance());
//		money.setText(info.getAppointment());//新月版本money显示距离
		time.setText(listConversationInfo.get(position).getCountDownTime()+"秒");
//		time.setText(info.getUpdated_at());
		
		//启动倒计时
		countBackwards(listConversationInfo.get(position),time);
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
	
	/**
	 * 倒计时
	 */
	private void countBackwards(final ConversationInfo tempConversation,final TextView time) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (tempConversation.getCountDownTime() >= 0) {
					// TODO Auto-generated method stub
					try {
						Thread.currentThread().sleep(1000);
						tempConversation.setCountDownTime(tempConversation.getCountDownTime()-1);
//						waitingHandler.sendMessage(MESSAGE_COUNTDOWN);
						time.setText(tempConversation.getCountDownTime()+"秒");
						handler.sendEmptyMessage(MESSAGE_COUNTDOWN);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MESSAGE_COUNTDOWN:
					
				break;
			}
		}
		
	};
}
