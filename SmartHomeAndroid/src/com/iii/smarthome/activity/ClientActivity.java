package com.iii.smarthome.activity;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.iii.smarthome.activity.FragmentLog;
import com.iii.smarthome.activity.FragmentSetting;
import com.iii.smarthome.service.ClientService;
import com.iii.smarthome.R;
import com.iii.smarthome.database.LanguagesDB;
import com.iii.smarthome.database.table.Language;
import com.iii.smarthome.webservice.ConfigServer;
import com.iii.smarthome.webservice.DataAccessHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ClientActivity extends Activity implements View.OnClickListener{
	private LinearLayout tab_setting,tab_log,tab_notify,tab_close;
	private FragmentTransaction fragMentTra = null;
	private FragmentNotify fragNotify;
	private FragmentLog fragLog;
	private FragmentSetting fragSetting;
	private String offerIMEI,myIMEI,offerName,myName;
	private LanguagesDB dB;
	private DataAccessHelper da = new DataAccessHelper(ConfigServer.WEBSERVICE);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkConnection();
		checkIMEI();
		initializeComponent();
	}
	private void initializeComponent() {
		//load language
		dB = new LanguagesDB(this);
		if(dB.getLanguageCode().isEmpty())dB.insert(new Language(getResources().getStringArray(R.array.language_name)[1],getResources().getStringArray(R.array.language_code)[1],getResources().obtainTypedArray(R.array.language_image).getResourceId(1, -1)));
	    Locale locale = new Locale(dB.getLanguageCode()); 
	    Locale.setDefault(locale);
	    Configuration config = new Configuration();
	    config.locale = locale;
	    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
	    //load config
		ConfigServer.serverConfig();
		setContentView(R.layout.activity_client);
		tab_log = (LinearLayout)findViewById(R.id.tab_log);tab_log.setOnClickListener(this);
		tab_notify = (LinearLayout)findViewById(R.id.tab_notify);tab_notify.setOnClickListener(this);
		tab_setting = (LinearLayout)findViewById(R.id.tab_setting);tab_setting.setOnClickListener(this);
		//tab_close = (LinearLayout)findViewById(R.id.tab_close);tab_close.setOnClickListener(this);
		tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
		tab_notify.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
		tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
		if(fragLog==null)fragLog = new FragmentLog();
		//fragChat.setMyProfile(myName, myIMEI, myNumber);
		fragMentTra = getFragmentManager().beginTransaction();
		fragMentTra.replace(R.id.container_client, fragLog);
		fragMentTra.commit();
	}

	private boolean isClientServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	private void startClientService(){
		if (!isClientServiceRunning(ClientService.class)) {
			Intent i = new Intent(getBaseContext(), ClientService.class);
			startService(i);
		}
	}
	private void checkConnection(){
		
	}
	private void checkIMEI(){
		try {
			// Check IMEI from web service
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			myIMEI = telephonyManager.getDeviceId();
			JSONArray jHouses = null;
			String result = da.responseString("imei", myIMEI);
			JSONObject json = new JSONObject(result);
			jHouses = json.getJSONArray("Houses");
			if (jHouses != null && jHouses.length() == 1) {
				this.myName = jHouses.getJSONObject(0).getString("room_number");
				this.myIMEI = jHouses.getJSONObject(0).getString("imei");
			}
			startClientService();
		} catch (JSONException ex) {
			Toast.makeText(this,R.string.error_device,Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	@Override
	protected void onDestroy() {
		// Disconnect automatically when the activity is destroyed.
		super.onDestroy();
	}

	public String[] getIMEI(){
		return new String[]{myIMEI,offerIMEI};
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tab_log:
			tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
			tab_notify.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			if(fragLog==null)fragLog = new FragmentLog();
			//fragLog.setMyProfile(myName, myIMEI, myNumber);
			fragMentTra = getFragmentManager().beginTransaction();
			fragMentTra.replace(R.id.container_client, fragLog);
			fragMentTra.commit();
			break;
		case R.id.tab_notify:
			tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_notify.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
			tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			if(fragNotify==null)fragNotify = new FragmentNotify();
			
			fragMentTra = getFragmentManager().beginTransaction();
			fragMentTra.replace(R.id.container_client, fragNotify);
			fragMentTra.commit();
			break;
		case R.id.tab_setting:
			tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_notify.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
			if(fragSetting==null)fragSetting = new FragmentSetting();
			
			fragMentTra = getFragmentManager().beginTransaction();
			fragMentTra.replace(R.id.container_client, fragSetting);
			fragMentTra.commit();
			break;
//		case R.id.tab_close:
//			finish();
//			break;
		default:
			break;
		}
	}
}
