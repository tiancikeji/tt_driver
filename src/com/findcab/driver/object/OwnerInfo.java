package com.findcab.driver.object;

import org.json.JSONObject;

public class OwnerInfo {
	private String car_license;//车牌照
	private String car_service_number;
	private String car_type;
	private double distance;
	private String created_at;//创建时间
	private int id;//用户唯一ID
	private double lat;//纬度
	private double lng;//经度
	private String mobile;//手机
	private String name;//用户名
	private String password;//密码
	private int rate;
	private String updated_at;
	
	public OwnerInfo(JSONObject jObject){
		car_license = jObject.optString("car_license");
		car_service_number = jObject.optString("car_service_number");
		car_type = jObject.optString("car_type");
		distance = jObject.optDouble("distance");
		id = jObject.optInt("id");
		lat = jObject.optDouble("lat");
		lng = jObject.optDouble("lng");
		mobile = jObject.optString("mobile");
		name = jObject.optString("name");
		password = jObject.optString("password");
		rate = jObject.optInt("rate");
		updated_at = jObject.optString("updated_at");
	}
	
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getRate() {
		return rate;
	}

	public String getCar_license() {
		return car_license;
	}

	public void setCar_license(String carLicense) {
		car_license = carLicense;
	}

	public String getCar_service_number() {
		return car_service_number;
	}

	public void setCar_service_number(String carServiceNumber) {
		car_service_number = carServiceNumber;
	}

	public String getCar_type() {
		return car_type;
	}

	public void setCar_type(String carType) {
		car_type = carType;
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
