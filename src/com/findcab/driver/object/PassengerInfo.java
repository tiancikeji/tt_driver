package com.findcab.driver.object;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 乘客注册信息
 * 
 * @author yuqunfeng
 * 
 */
public class PassengerInfo {

	private String created_at;
	private int id;
	private double lat;
	private double lng;
	private String mobile;
	private String name;
	private String password;
	private String updated_at;
	private int online;

	public PassengerInfo(JSONObject jObject) {

		try {
			created_at = jObject.getString("created_at");
			id = jObject.getInt("id");
			lat = jObject.getDouble("lat");
			lng = jObject.getDouble("lng");
			mobile = jObject.getString("mobile");
			name = jObject.getString("name");
			password = jObject.getString("password");
			updated_at = jObject.getString("updated_at");
			online = jObject.getInt("online");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String createdAt) {
		created_at = createdAt;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updatedAt) {
		updated_at = updatedAt;
	}

}
