package com.findcab.driver.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DBBase {

	public static final String _ID = "_id";//自增列
	public Context context;
	protected SQLiteDatabase sqliteDatabase;
	protected static DAOHelper daoHelper;
	
	public DBBase(Context context){
		this.context = context;
		DBBase.daoHelper = new DAOHelper(context,DAOHelper.DB_NAME,null, DAOHelper.DATABASRE_VERSION);
	}
	
	public void open(){
		if(this.sqliteDatabase == null){
			this.sqliteDatabase = daoHelper.getWritableDatabase();
		}
	}
	
	public void close(){
		sqliteDatabase.close();
	}
	
	public boolean isOpen(){
		if(sqliteDatabase != null){
			return sqliteDatabase.isOpen();
		}
		return false;
	}
	
	public void beginTransaction(){
		sqliteDatabase.beginTransaction();
	}
	
	public void endTransaction(){
		sqliteDatabase.endTransaction();
	}
}
