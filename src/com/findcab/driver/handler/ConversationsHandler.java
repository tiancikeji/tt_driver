package com.findcab.driver.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;

import com.findcab.driver.object.ConversationInfo;

/**
 * 得到最最近一次会话
 * 
 * @author yuqunfeng
 * 
 */
public class ConversationsHandler extends AbsHandler {

	int index;

	public ConversationsHandler(int index) {
		this.index = index;
	}
	public ConversationsHandler() {
		
	}

	@Override
	public Object parseResponse(String responseStr) {
		// TODO Auto-generated method stub

		ConversationInfo info = null;

		List<ConversationInfo> list = new ArrayList<ConversationInfo>();
		try {
			JSONObject object = new JSONObject(responseStr);
			JSONArray array = object.getJSONArray("conversations");

			// 得到最最近一次会话

			 for (int i = 0; i < array.length(); i++) {
			
			 JSONObject oJsonObject = array.getJSONObject(i);
			
			 info = new ConversationInfo(oJsonObject);
			
			 if (oJsonObject.getInt("status") == 0) {
			 list.add(info);
			 }
			 }
			
			
			
			
			

//			if (index == -1) {
//				index = array.length() - 1;
//				info = new ConversationInfo(array.getJSONObject(index));
//				info.setIndex(index);
//			} else {
//
//				info = new ConversationInfo(array.getJSONObject(index));
//				info.setIndex(index);
//
//			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 司机应答

		//return info;
		return list;

	}
}
