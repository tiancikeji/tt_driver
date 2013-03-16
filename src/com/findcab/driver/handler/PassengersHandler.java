package com.findcab.driver.handler;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.findcab.driver.object.PassengerInfo;

/**
 * 获得在线乘客
 * 
 * @author yuqunfeng
 * 
 */
public class PassengersHandler extends AbsHandler {

	@Override
	public Object parseResponse(String responseStr) {
		// TODO Auto-generated method stub
		List<PassengerInfo> listInfo = null;
		try {
			JSONObject jsonObject = new JSONObject(responseStr);

			JSONArray array = jsonObject.getJSONArray("passengers");

			listInfo = new ArrayList<PassengerInfo>();
			PassengerInfo passengerInfo;
			for (int i = 0; i < array.length(); i++) {
				passengerInfo = new PassengerInfo(array.getJSONObject(i));
				if (passengerInfo.getOnline() == 1) {

					listInfo.add(passengerInfo);

				}

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return listInfo;

	}
}
