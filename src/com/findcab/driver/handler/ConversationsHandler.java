package com.findcab.driver.handler;

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

	@Override
	public Object parseResponse(String responseStr) {
		// TODO Auto-generated method stub

		ConversationInfo lastConversation = null;
		try {
			JSONObject object = new JSONObject(responseStr);
			JSONArray array = object.getJSONArray("conversations");

			// 得到最最近一次会话

			if (index == -1) {
				index = array.length() - 1;
				lastConversation = new ConversationInfo(array
						.getJSONObject(index));
				lastConversation.setIndex(index);
			} else {

				lastConversation = new ConversationInfo(array
						.getJSONObject(index));
				lastConversation.setIndex(index);

			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// 司机应答

		return lastConversation;

	}
}
