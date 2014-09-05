package com.iii.facetoface.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iii.facetoface.R;
import com.iii.facetoface.database.LoginDB;
import com.iii.facetoface.database.table.User;
import com.iii.facetoface.service.ClientService;
import com.iii.facetoface.webservice.ConfigServer;
import com.iii.facetoface.webservice.DataAccessHelper;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
	private LoginDB lgDB;
	private String myIMEI;
	private DataAccessHelper da;
	EditText txt_phone;
	EditText txt_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// config server
		ConfigServer.serverConfig();

		// check logined
		lgDB = new LoginDB(LoginActivity.this);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		myIMEI = telephonyManager.getDeviceId();
		da = new DataAccessHelper(ConfigServer.WEBSERVICE);

		// if exists in SQLite
		if (lgDB.getNumRow() == 1) {
			ArrayList<User> alUser = lgDB.getUser();
			String phone_number = alUser.get(0).getNumber();
			String imei = alUser.get(0).getIMEI();
			String user_name =  alUser.get(0).getName();
			JSONArray jHouses = null;
			String[] key = { "phone_number", "user_name", "mobile_imei" };
			String[] value = { phone_number,user_name, imei };

			String result = "";
			try {
				result = da.responseString(key, value);
				JSONObject json = new JSONObject(result);
				jHouses = json.getJSONArray("HouseMobile");
			} catch (JSONException e1) {
				stopService(new Intent(getBaseContext(),ClientService.class));
				lgDB.delete(myIMEI);
				txt_phone = (EditText) findViewById(R.id.log_txt_phone);
				txt_name = (EditText) findViewById(R.id.log_txt_name);
				Button btn_login = (Button) findViewById(R.id.log_btn_login);

				btn_login.setOnClickListener(this);
				
			}

			if (jHouses != null) {
				if (jHouses.length() == 1) {

					transaction(user_name, imei, phone_number);

				}
			} 
		} else {
			// check registed
			JSONArray jCheckReg = null;
			String result1 = "";

			try {
				result1 = da.responseString("check_imei", myIMEI);
				JSONObject js = new JSONObject(result1);
				jCheckReg = js.getJSONArray("User_info");
				String phone_number = jCheckReg.getJSONObject(0).getString("phone_number");
				String imei = jCheckReg.getJSONObject(0).getString("imei");
				String user_name = jCheckReg.getJSONObject(0).getString("user_name");
				User us = new User(user_name, phone_number, imei);
				lgDB.insert(us);
				transaction(user_name, imei, phone_number);

			} catch (JSONException e2) {
				Log.e("WTF", e2.toString());
				//
				txt_phone = (EditText) findViewById(R.id.log_txt_phone);
				txt_name = (EditText) findViewById(R.id.log_txt_name);
				Button btn_login = (Button) findViewById(R.id.log_btn_login);

				btn_login.setOnClickListener(this);
			}

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (txt_phone.getText().toString().trim().equals("")
				|| txt_name.getText().toString().trim().equals("")) {
			Toast.makeText(LoginActivity.this, "Bạn cần nhập đủ thông tin!",
					Toast.LENGTH_LONG).show();
		} else {

			JSONArray jHouses = null;
			String[] key = { "phone_number", "user_name", "mobile_imei" };
			String[] value = { txt_phone.getText().toString(),
					txt_name.getText().toString(), myIMEI };

			String result = "";
			try {
				result = da.responseString(key, value);
				JSONObject json = new JSONObject(result);
				jHouses = json.getJSONArray("HouseMobile");
			} catch (JSONException e1) {
				Log.e("test", e1.toString());
				Toast.makeText(LoginActivity.this,
						"Số điện thoại sai hoặc đã được đăng ký",
						Toast.LENGTH_LONG).show();
			}

			if (jHouses != null) {
				if (jHouses.length() == 1) {

					try {
						String phone_number = jHouses.getJSONObject(0).getString("phone_number");
						String imei = jHouses.getJSONObject(0).getString("imei");
						String user_name = jHouses.getJSONObject(0).getString("user_name");
						User us = new User(user_name, phone_number, imei);
						lgDB.insert(us);
						transaction(user_name, imei, phone_number);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

	}

	// transction activity
	private void transaction(String user_name, String imei, String phone_number) {
		Bundle bundle = new Bundle();
		bundle.putString("myName", user_name);
		bundle.putString("myIMEI", imei);
		bundle.putString("myNumber", phone_number);
		Intent i = new Intent(LoginActivity.this, ClientActivity.class);
		i.putExtras(bundle);
		startActivity(i);
		finish();
	}
}
