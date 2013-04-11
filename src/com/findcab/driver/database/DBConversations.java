package com.findcab.driver.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DBConversations extends DBBase{

	public static final String tb_name = "tb_conversations";
	public static final String con_content = "con_content";
	public static final String con_created_at = "con_created_at";
	public static final String con_from_id = "con_from_id";
	public static final String con_id = "con_id";
	public static final String con_status = "con_status";
	public static final String con_status_desc = "con_status_desc";
	public static final String con_to_id = "con_to_id";
	public static final String con_trip_id = "con_trip_id";
	public static final String con_updated_at = "con_updated";
	
	public DBConversations(Context context) {
		super(context);
		
	}
	
	public Cursor getAllMyConversations(){
		return this.sqliteDatabase.query(tb_name, null, null, null, null, null, null);
	}
	
	public void deleteAllMyConversations(){
		String sqlCmd = "delete from " + tb_name;
		sqliteDatabase.execSQL(sqlCmd);
	}
	
	public long saveConversation(ContentValues values){
		return this.sqliteDatabase.insert(tb_name, null, values);
		
	}
	
	public Cursor getConversations(String id){
		String where = con_from_id + "=?";
		return sqliteDatabase.query(tb_name, null, where, null, null, null, null);
	}

}
