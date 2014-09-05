package com.iii.facetoface.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ToggleButton;

public class ConfigDB extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "SmartHome";

	// ------ bang LOGIN -------//
	public static final String TABLE_LOGIN = "LOGIN";
	public static final String L_ID = "ID";
	public static final String L_NAME = "NAME";
	public static final String L_IMEI= "IMEI";
	public static final String L_PHONE= "PHONE";
	
	// ------ bang Languages ------//
	public static final String TABLE_LG = "LANGUAGE";
	public static final String LG_ID = "ID";
	public static final String LG_NAME = "NAME";
	public static final String LG_CODE = "CODE";
	
	// ------ bang Ringtones -------//
	public static final String TABLE_RT = "RING_TONE";
	public static final String RT_ID = "ID";
	public static final String RT_NAME = "NAME";
	public static final String RT_STATUS = "STATUS";
	public static final String RT_PATH = "PATH";
	public static final String RT_TIME_PLAY = "TIME_PLAY";

	public ConfigDB(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// ---- create ringtones table ---- //
		String ringtones_create = "create table " + TABLE_RT + " ( " + RT_ID
				+ " integer primary key autoincrement, " + RT_NAME
				+ " text not null, " + RT_PATH
				+ " text not null," + RT_STATUS + " boolean default 0,"+RT_TIME_PLAY+" integer default 30)";
		db.execSQL(ringtones_create);

		// --- create languages table --- //
		String languages_create = "create table " + TABLE_LG + " ( " + LG_ID
				+ " integer primary key autoincrement , " + LG_NAME + " text not null , "
				+ LG_CODE + " text not null)";
		db.execSQL(languages_create);

 
		// ---- create login table ---- //
		String login_create = "create table " + TABLE_LOGIN + " ( " + L_ID
				+ " integer primary key autoincrement, " 
				+ L_NAME + " text not null, " 
				+ L_IMEI + " text not null," 
				+ L_PHONE+ " text not null)";
		db.execSQL(login_create);
		
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop if table exists " + TABLE_RT);
		db.execSQL("drop if table exists " + TABLE_LG);
		db.execSQL("drop if table exists " + TABLE_LOGIN);
		onCreate(db);
	}

}
