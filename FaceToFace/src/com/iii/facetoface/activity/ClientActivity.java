package com.iii.facetoface.activity;

import com.iii.facetoface.R;
import com.iii.facetoface.service.ClientService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.LinearLayout;
@SuppressLint("NewApi")
public class ClientActivity extends Activity implements OnClickListener{
	private LinearLayout tab_chat,tab_setting,tab_log,tab_close;
	private FragmentTransaction fragMentTra = null;
	private FragmentChat fragChat;
	private FragmentLog fragLog;
	private FragmentSetting fragSetting;
	private String offerIMEI,myIMEI,offerName,myName,myNumber,offerNumber;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initializeComponent();
		tab_chat = (LinearLayout)findViewById(R.id.tab_chat);tab_chat.setOnClickListener(this);
		tab_log = (LinearLayout)findViewById(R.id.tab_log);tab_log.setOnClickListener(this);
		tab_setting = (LinearLayout)findViewById(R.id.tab_setting);tab_setting.setOnClickListener(this);
		tab_close = (LinearLayout)findViewById(R.id.tab_close);tab_close.setOnClickListener(this);
		tab_chat.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
		tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
		tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
		if(fragChat==null)fragChat = new FragmentChat();
		fragChat.setMyProfile(myName, myIMEI, myNumber);
		fragMentTra = getFragmentManager().beginTransaction();
		fragMentTra.replace(R.id.container, fragChat);
		fragMentTra.commit();
		//start client service
		if(!isClientServiceRunning(ClientService.class))startClientService();
	}
	private void initializeComponent() {
		setContentView(R.layout.activity_client);	
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myName = bundle.getString("myName");
			myIMEI = bundle.getString("myIMEI");
			myNumber = bundle.getString("myNumber");
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tab_chat:
			tab_chat.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
			tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			if(fragChat==null)fragChat = new FragmentChat();
			fragChat.setMyProfile(myName, myIMEI, myNumber);
			fragMentTra = getFragmentManager().beginTransaction();
			fragMentTra.replace(R.id.container, fragChat);
			fragMentTra.commit();
			break;
		case R.id.tab_log:
			tab_chat.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
			tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			if(fragLog==null)fragLog = new FragmentLog();
			
			fragMentTra = getFragmentManager().beginTransaction();
			fragMentTra.replace(R.id.container, fragLog);
			fragMentTra.commit();
			break;
		case R.id.tab_setting:
			tab_chat.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_log.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background));
			tab_setting.setBackground(getResources().getDrawable(R.drawable.actionbar_tab_background_selected));
			if(fragSetting==null)fragSetting = new FragmentSetting();
			
			fragMentTra = getFragmentManager().beginTransaction();
			fragMentTra.replace(R.id.container, fragSetting);
			fragMentTra.commit();
			break;
		case R.id.tab_close:
			finish();
			break;
		default:
			break;
		}
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
		Intent i = new Intent(getBaseContext(),ClientService.class);
		startService(i);
	}

}
