package com.iii.facetoface.database;

import java.util.ArrayList;

import com.iii.facetoface.database.table.Ringtone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RingTonesDB extends ConfigDB {
	public RingTonesDB(Context context) {
		super(context);
		
		// TODO Auto-generated constructor stub
	}
	
    public long insert(String name,Boolean status,String path){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(ConfigDB.RT_NAME, name);
    	values.put(ConfigDB.RT_STATUS, status);
    	values.put(ConfigDB.RT_PATH, path);
    	return dB.insert(ConfigDB.TABLE_RT, null, values);
    }
    public Boolean delete(int rt_Id){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	return dB.delete(ConfigDB.TABLE_RT, ConfigDB.RT_ID +"="+rt_Id, null) > 0;
    }
    public long insert(Ringtone r){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(ConfigDB.RT_NAME, r.getName());
    	values.put(ConfigDB.RT_STATUS, true);
    	values.put(ConfigDB.RT_PATH, r.getPath());
    	return dB.insert(ConfigDB.TABLE_RT, null, values);
    }
    public void delete(){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	dB.delete(ConfigDB.TABLE_RT,"1", null);
    	dB.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME ='"+ConfigDB.TABLE_RT+"'");
    }
    public void update(Ringtone r){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	dB.execSQL("UPDATE "+ConfigDB.TABLE_RT+" SET "+ConfigDB.RT_PATH+" = '"+r.getPath()+"',"+
    				ConfigDB.RT_NAME+" = '"+r.getName()+"',"+ConfigDB.RT_TIME_PLAY+"='"+r.getTime_play()+"',"+
    				ConfigDB.RT_STATUS+"= 1 WHERE "+ConfigDB.RT_ID+"= 1");
    }
    public void selectRingtone(Ringtone r){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	int status = 1;
    	if(!r.getStatus())status = 0;
    	dB.execSQL("UPDATE "+ConfigDB.TABLE_RT+" SET "+ConfigDB.RT_STATUS+"= '"+status+"' WHERE "+ConfigDB.RT_ID+"= 1");
    }
	public int getNumRow(){
		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "SELECT * FROM "+ConfigDB.TABLE_RT;
		Cursor c = dB.rawQuery(sql, null);
		int count = c.getCount();
		c.close();
		return count;
				
	}
	//---------------------------------------------------//
	public ArrayList<Ringtone> getRingtones(){
		ArrayList<Ringtone> result = new ArrayList<Ringtone>();
		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "SELECT * FROM "+ConfigDB.TABLE_RT;
		Cursor c = dB.rawQuery(sql, null);
		if (c.moveToFirst()) {
			do {
				Ringtone r = new Ringtone(c.getInt(0),c.getString(1), c.getString(2), c.getInt(3), c.getInt(4));
				result.add(r);
			} while (c.moveToNext());
			
		}
		c.close();
		return result;
	}
	public Boolean getRingType(){
		//------------ true: sdcard, false:default in raw---------------//
		for(Ringtone r:getRingtones()){
			if(r.getStatus()) return true;
		}
		return false;
		
	}
	public String getLocation(){
		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "SELECT * FROM "+ConfigDB.TABLE_RT+" WHERE "+ConfigDB.RT_STATUS+" = 1";
		Cursor c = dB.rawQuery(sql, null);
		if(c.getCount() ==1){
			
			if (c.moveToFirst()) {
				String result = c.getString(2)+"/"+c.getString(1)+".mp3";
				return result;
			}
		}
		c.close();
		return null;
	}
}
