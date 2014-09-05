package com.iii.smarthome.database;

import com.iii.smarthome.database.table.Language;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LanguagesDB extends ConfigDB {
	public LanguagesDB(Context context) {
		super(context);
		
		// TODO Auto-generated constructor stub
		
	}
	
    public void insert(String name,Boolean check,String code,String resource){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(ConfigDB.LG_NAME, name);
    	values.put(ConfigDB.LG_CODE,code);
    	dB.insert(ConfigDB.TABLE_LG, null, values);
    	dB.close();
    }

    public void insert(Language language){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	ContentValues values = new ContentValues();
    	values.put(ConfigDB.LG_NAME, language.getName());
    	values.put(ConfigDB.LG_CODE, language.getCode());
    	dB.insert(ConfigDB.TABLE_LG, null, values);
    	dB.close();
    }

    public void update(Language language){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	dB.execSQL("UPDATE "+ConfigDB.TABLE_LG +" SET "+ConfigDB.LG_NAME +"= '"+language.getName()+"',"+
    			ConfigDB.LG_CODE +"= '"+language.getCode()+"' "+"WHERE "+ ConfigDB.LG_ID +"=1");
    	dB.close();
    }
    public String getLanguageCode(){
    	SQLiteDatabase dB = this.getWritableDatabase();
    	String result = "";
    	Cursor c = dB.rawQuery("SELECT * FROM "+ConfigDB.TABLE_LG, null);
    	if(c.moveToFirst()){
    		do{
    			result = c.getString(c.getColumnIndex(ConfigDB.LG_CODE));
    		}while(c.moveToNext());
    	}
    	c.close();
    	dB.close();
    	return result;
    }
}
