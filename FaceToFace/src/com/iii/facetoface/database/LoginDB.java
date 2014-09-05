package com.iii.facetoface.database;

import java.util.ArrayList;

import com.iii.facetoface.database.table.Ringtone;
import com.iii.facetoface.database.table.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LoginDB extends ConfigDB {
	public LoginDB(Context context) {
		super(context);

		// TODO Auto-generated constructor stub
	}

	public long insert(User user) {
		SQLiteDatabase dB = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ConfigDB.L_NAME, user.getName());
		values.put(ConfigDB.L_IMEI, user.getIMEI());
		values.put(ConfigDB.L_PHONE, user.getNumber());
		return dB.insert(ConfigDB.TABLE_LOGIN, null, values);
	}

	public Boolean delete(String l_imei) {
		SQLiteDatabase dB = this.getWritableDatabase();
		return dB.delete(ConfigDB.TABLE_LOGIN, ConfigDB.L_IMEI + "=" + l_imei, null) > 0;
	}

	public void update(String name) {
		SQLiteDatabase dB = this.getWritableDatabase();
		dB.execSQL("UPDATE " + ConfigDB.TABLE_LOGIN + " SET " + ConfigDB.L_NAME
				+ " = '" + name + "'");
	}

	public int getNumRow() {
		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "SELECT * FROM "+ConfigDB.TABLE_LOGIN;
		Cursor c = dB.rawQuery(sql, null);
		int count = c.getCount();
		c.close();
		return count;

	}

	// ---------------------------------------------------//
	public ArrayList<User> getUser() {
		ArrayList<User> result = new ArrayList<User>();

		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "SELECT * FROM "+ConfigDB.TABLE_LOGIN;
		Cursor c = dB.rawQuery(sql, null);
		if (c.moveToNext()) {
			User us = new User(c.getString(1), c.getString(3), c.getString(2));
			result.add(us);

		}
		c.close();
		return result;
	}

}
