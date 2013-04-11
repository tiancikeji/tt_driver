package com.findcab.driver.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DAOHelper extends SQLiteOpenHelper{
	public static final String DB_NAME = "TiantiandacheSQLite.db";
	public static final int DATABASRE_VERSION = 1;
	
	public DAOHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("Create table " + DBConversations.tb_name + " (" + DBBase._ID
				+ " integer primary key, " + DBConversations.con_content + " text,"
				+ DBConversations.con_created_at + " text,"
				+ DBConversations.con_from_id + " text,"
				+ DBConversations.con_id + " text,"
				+ DBConversations.con_status + " text,"
				+ DBConversations.con_status_desc + " text,"
				+ DBConversations.con_to_id + " text,"
				+ DBConversations.con_trip_id + " text,"
				+ DBConversations.con_updated_at + " text)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
		
	}
}
