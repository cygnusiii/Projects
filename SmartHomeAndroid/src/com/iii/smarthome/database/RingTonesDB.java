package com.iii.smarthome.database;

import java.util.ArrayList;

import com.iii.smarthome.database.table.Ringtone;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RingTonesDB extends ConfigDB {
	public RingTonesDB(Context context) {
		super(context);

		// TODO Auto-generated constructor stub
	}

	public void delete(Ringtone r) {
		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "DELETE FROM " + TABLE_RT + " WHERE " + RT_ID + "="
				+ r.getType();
		dB.execSQL(sql);
		dB.close();
	}

	public void insertRingtone(Ringtone r) {
		SQLiteDatabase dB = this.getWritableDatabase();
		String sql = "INSERT INTO " + TABLE_RT + " VALUES('" + r.getType()
				+ "','" + r.getName() + "','" + r.getPath() + "','"
				+ r.getTime() + "')";
		Cursor c = dB.rawQuery("SELECT * FROM " + TABLE_RT + " WHERE " + RT_ID
				+ "=" + r.getType(), null);
		if (c.getCount() > 0) {
			sql = "UPDATE " + TABLE_RT + " SET " + RT_NAME + "='" + r.getName()
					+ "'," + RT_PATH + "='" + r.getPath() + "'," + RT_TIME_PLAY
					+ "='" + r.getTime() + "' WHERE " + RT_ID + "="
					+ r.getType();
		}
		dB.execSQL(sql);
		c.close();
		dB.close();
	}

	public String getLocationRingtone() {
		return getLocation(1);
	}

	public String getLocationMessage() {
		return getLocation(2);
	}

	public String getLocationNotify() {
		return getLocation(3);
	}

	public String getLocation(int type) {
		String result = "";
		SQLiteDatabase dB = this.getReadableDatabase();
		Cursor c = dB.rawQuery("SELECT * FROM " + TABLE_RT + " WHERE " + RT_ID
				+ "=" + type, null);
		if (c.moveToFirst()) {
			do {
				String name = c.getString(c.getColumnIndex(RT_NAME));
				String path = c.getString(c.getColumnIndex(RT_PATH));
				result = path + name + ".mp3";
			} while (c.moveToNext());
		}
		c.close();
		dB.close();
		return result;
	}

	public int getTimeRingtone() {
		return getTime(1);
	}

	public int getTimeMessage() {
		return getTime(2);
	}

	public int getTimeNotify() {
		return getTime(3);
	}

	public int getTime(int type) {
		int result = 0;
		SQLiteDatabase dB = this.getReadableDatabase();
		Cursor c = dB.rawQuery("SELECT * FROM " + TABLE_RT + " WHERE " + RT_ID
				+ "=" + type, null);
		if (c.moveToFirst()) {
			do {
				result = c.getInt(c.getColumnIndex(RT_TIME_PLAY));
			} while (c.moveToNext());
		}
		c.close();
		dB.close();
		return result;
	}

	public ArrayList<Ringtone> getRingtones() {
		SQLiteDatabase dB = this.getReadableDatabase();
		ArrayList<Ringtone> result = new ArrayList<Ringtone>();
		Cursor c = dB.rawQuery("SELECT * FROM " + TABLE_RT, null);
		if (c.moveToFirst()) {
			do {
				String name = c.getString(c.getColumnIndex(RT_NAME));
				String path = c.getString(c.getColumnIndex(RT_PATH));
				int type = c.getInt(c.getColumnIndex(RT_ID));
				int time = c.getInt(c.getColumnIndex(RT_TIME_PLAY));
				result.add(new Ringtone(name, path, time, type));
			} while (c.moveToNext());
		}
		c.close();
		dB.close();
		return result;
	}
}
