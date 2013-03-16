package com.findcab.driver.object;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 请求
 * 
 * @author yuqunfeng
 * 
 */
public class ConversationInfo {

	private String content;
	private String created_at;
	private int from_id;
	private int id;
	private String status_desc;
	private int status;
	private int to_id;
	private int trip_id;
	private String updated_at;
	private int index;

	public ConversationInfo(JSONObject jObject) {

		try {
			content = jObject.getString("content");
			created_at = jObject.getString("created_at");
			from_id = jObject.getInt("from_id");
			id = jObject.getInt("id");
			status = jObject.getInt("status");
			status_desc = jObject.getString("status_desc");
			to_id = jObject.getInt("to_id");
			trip_id = jObject.getInt("trip_id");

			updated_at = jObject.getString("updated_at");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updatedAt) {
		updated_at = updatedAt;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getFrom_id() {
		return from_id;
	}

	public void setFrom_id(int fromId) {
		from_id = fromId;
	}

	public String getStatus_desc() {
		return status_desc;
	}

	public void setStatus_desc(String statusDesc) {
		status_desc = statusDesc;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getTo_id() {
		return to_id;
	}

	public void setTo_id(int toId) {
		to_id = toId;
	}

	public int getTrip_id() {
		return trip_id;
	}

	public void setTrip_id(int tripId) {
		trip_id = tripId;
	}

}
