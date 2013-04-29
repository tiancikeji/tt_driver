package com.findcab.driver.object;

import java.io.Serializable;

import org.json.JSONObject;

/**
 * 路线信息
 * 
 * @author yuqunfeng
 * 
 */
public class TripsInfo implements Serializable {

	/**
	 * 
	 */
	private static long serialVersionUID = 8246601128965437499L;
	/**
	 * 
	 */

	private String passenger_id;
	private String start;
	private double start_lat;
	private double start_lng;

	private String end;
	private double end_lat;
	private double end_lng;
	private String appointment;
	private String updated_at;
	private String created_at;
	
	
	private int id;

	public TripsInfo(JSONObject jObject) {

		passenger_id = jObject.optString("passenger_id");
		start = jObject.optString("start");
		start_lat = jObject.optDouble("start_lat");
		start_lng = jObject.optDouble("start_lng");

		end = jObject.optString("end");
		end_lat = jObject.optDouble("end_lat");
		end_lng = jObject.optDouble("end_lng");

		id = jObject.optInt("id");
		appointment = jObject.optString("price");
		updated_at = jObject.optString("updated_at");
		created_at = jObject.optString("created_at");
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static void setSerialversionuid(long serialversionuid) {
		serialVersionUID = serialversionuid;
	}

	public String getPassenger_id() {
		return passenger_id;
	}

	public void setPassenger_id(String passengerId) {
		passenger_id = passengerId;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public static void setSerialVersionUID(long serialVersionUID) {
		TripsInfo.serialVersionUID = serialVersionUID;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updatedAt) {
		updated_at = updatedAt;
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

	public double getStart_lat() {
		return start_lat;
	}

	public void setStart_lat(double startLat) {
		start_lat = startLat;
	}

	public double getStart_lng() {
		return start_lng;
	}

	public void setStart_lng(double startLng) {
		start_lng = startLng;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public double getEnd_lat() {
		return end_lat;
	}

	public void setEnd_lat(double endLat) {
		end_lat = endLat;
	}

	public double getEnd_lng() {
		return end_lng;
	}

	public void setEnd_lng(double endLng) {
		end_lng = endLng;
	}

	public String getAppointment() {
		return appointment;
	}

	public void setAppointment(String appointment) {
		this.appointment = appointment;
	}
	
}
