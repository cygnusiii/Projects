package com.iii.smarthome.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iii.smarthome.R;
import com.iii.smarthome.activity.ClientActivity;
import com.iii.smarthome.activity.VideoActivity;
import com.iii.smarthome.conference.AppChat;
import com.iii.smarthome.database.table.Message;
import com.iii.smarthome.webservice.ConfigChannel;
import com.iii.smarthome.webservice.ConfigServer;
import com.iii.smarthome.webservice.DataAccessHelper;
import com.iii.smarthome.webservice.Encrypt;

import fm.Encoding;
import fm.Serializer;
import fm.SingleAction;
import fm.icelink.BaseLinkArgs;
import fm.icelink.Stream;
import fm.icelink.StreamFormat;
import fm.icelink.StreamLinkReceiveRTPArgs;
import fm.icelink.StreamType;
import fm.icelink.websync.BaseLinkArgsExtensions;
import fm.websync.Record;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class ClientService extends Service {
	private boolean useWebSyncExtension = true;
	private AppChat appChat;
	private BroadcastReceiver receiver;
	private String myName,myIMEI;
	private ArrayList<String> users = new ArrayList<String>();
	private boolean isVideoCall,isVideoComming;
	private DataAccessHelper da = new DataAccessHelper(ConfigServer.WEBSERVICE);
	private NotificationManager NM;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		try {
			Toast.makeText(this, "SmartHome service start ...", Toast.LENGTH_LONG).show();
			TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			myIMEI = telephonyManager.getDeviceId();
			// Start device client
			JSONArray jHouses =null;		
			String result = da.responseString("imei", myIMEI);
			JSONObject json = new JSONObject(result);
			jHouses =  json.getJSONArray("Houses");
			if(jHouses!=null&&jHouses.length()==1){
				myName = jHouses.getJSONObject(0).getString("room_number");
				myIMEI = jHouses.getJSONObject(0).getString("imei");
				ConfigServer.serverConfig();
				appChat = AppChat.getInstance();
				startChat(ConfigChannel.PUBLIC_CHANNEL);
			}else{
				this.stopSelf();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@SuppressWarnings("deprecation")
	public void notifyMessages() {
		String title = getResources().getString(R.string.administrator_notify);
		String subject = "";
		String body = "";
		NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notify = new Notification(com.iii.smarthome.R.drawable.avatar, title,System.currentTimeMillis());
		PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(ClientService.this,ClientActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
		notify.setLatestEventInfo(getApplicationContext(), subject, body,pending);
		notify.sound = Uri.parse("android.resource://"+ getPackageName() + "/" +com.iii.smarthome.R.raw.ringtone);
		notify.vibrate = new long[]{0,100,200,300};
		NM.notify(0, notify);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
		Toast.makeText(this, "SmartHome service stop ...", Toast.LENGTH_LONG).show();
	}
	private boolean isClientTop(Class<?> clientClass) {
		boolean flag = false;
	    ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE); 
	    String className = manager.getRunningTasks(1).get(0).topActivity.getClassName();   
	    if(className.equals(clientClass.getName()))
	    	flag = true;
	   
	    return flag;
	}
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);
		restartService();

	}
	private void restartService(){
		 Intent restartService = new Intent(getApplicationContext(), this.getClass());
		 restartService.setPackage(getPackageName());
		 PendingIntent restartServicePI = PendingIntent.getService(getApplicationContext(), 1, restartService,PendingIntent.FLAG_ONE_SHOT);
		 AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		 alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);
	}
	private void receiveMessage(){
		appChat.getTextStream().addOnLinkReceiveRTP(new SingleAction<StreamLinkReceiveRTPArgs>() {
			public void invoke(StreamLinkReceiveRTPArgs e) {

				final String offerMessage = Encoding.getUTF8().getString(e.getPacket().getPayload());
				final String offerName = getPeerName(e);
				final String offerIMEI = getPeerImei(e);
				//Nháº­n thÃ´ng bÃ¡o busy tá»« answer
				if (Encrypt.Compare(offerMessage, ConfigChannel.ANSWER_BUSY+ myIMEI)) {
					isVideoCall = isVideoComming = false;
					//gui broadcast cho fragmentclient
					if(isClientTop(VideoActivity.class)){
						Intent intent = new Intent("com.iii.smarthome.action.responseClient");
						intent.putExtra("serviceData",ConfigChannel.ANSWER_BUSY+offerName); 
						sendBroadcast(intent);
						
					}
					
				}
				//NhÃ¢n thÃ´ng bÃ¡o tá»« chá»‘i tá»« answer
				if (Encrypt.Compare(offerMessage, ConfigChannel.NO_ANSWER+ myIMEI)) {
					isVideoCall = isVideoComming = false;
					//gui broadcast cho fragmentclient
					if(isClientTop(VideoActivity.class)){
						Intent intent = new Intent("com.iii.smarthome.action.responseClient");
						intent.putExtra("serviceData",ConfigChannel.NO_ANSWER+offerName); 
						sendBroadcast(intent);
					}				
				}
				//Ngáº¯t cuá»™c gá»�i tá»« answer
				if (Encrypt.Compare(offerMessage, ConfigChannel.OFF_ANSWER+ myIMEI)) {
					isVideoCall = isVideoComming = false;
					//gui broadcast cho fragmentclient
					if(isClientTop(VideoActivity.class)){
						Intent intent = new Intent("com.iii.smarthome.action.responseClient");
						intent.putExtra("serviceData",ConfigChannel.OFF_ANSWER+offerName); 
						sendBroadcast(intent);
					}
					
				}
				if (Encrypt.Compare(offerMessage, ConfigChannel.OFFER_VIDEO+ myIMEI)) {
					if(!isVideoCall&&!isVideoComming){
						isVideoComming = true;
						if(!isClientTop(VideoActivity.class)){
							//goi activity client 
							 Intent i = new Intent(ClientService.this, VideoActivity.class);
							 i.putExtra("myName", myName);
							 i.putExtra("myIMEI", myIMEI);
							 i.putExtra("offerName", offerName);
							 i.putExtra("offerIMEI", offerIMEI);
							 i.putExtra("isComming", true);
							 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							 startActivity(i);
						}
						//gui broadcast cho fragmentclient
//						Intent intent = new Intent("com.iii.smarthome.action.responseClient");
//						intent.putExtra("serviceData",ConfigChannel.OFFER_VIDEO+offerName); 
//						intent.putExtra("offerIMEI",offerIMEI);
//						sendBroadcast(intent);
					

					}else{
						//Ä‘ang conference vá»›i ngÆ°á»�i khÃ¡c 
						appChat.sendMessage( Encrypt.getMD5(ConfigChannel.ANSWER_BUSY+ offerIMEI));
					}
				}
				if(Encrypt.Compare(offerMessage, ConfigChannel.NOTIFY_ALL)){
					if(isClientTop(ClientActivity.class)){
						Intent intent = new Intent("com.iii.smarthome.action.responseClient");
						intent.putExtra("serviceData","notifies"); 
						sendBroadcast(intent);
					}
					if(!getNotifies().isEmpty()){
						notifyMessages();
					}
					
				}
				
				if(Encrypt.Compare(offerMessage, myIMEI+ConfigChannel.CALL_LOG)){
					if(isClientTop(ClientActivity.class)){
						Intent intent = new Intent("com.iii.smarthome.action.responseClient");
						intent.putExtra("serviceData","call-log"); 
						sendBroadcast(intent);
					}
						notifyMessages();
					
					
				}
					
			}
		});
	}
	
	
	private ArrayList<Message> getNotifies(){
		ArrayList<Message> result = new ArrayList<Message>();
		String response = da.responseString("get_notifies", myIMEI);
		
		try {
			JSONObject json = new JSONObject(response);
			JSONArray jNotifies  = json.getJSONArray("Notifies");
			for(int i= 0 ;i<jNotifies.length();i++){
				JSONObject o = jNotifies.getJSONObject(i);
				Message m = new Message();
				m.setId(o.getInt("id"));
				m.setTitle(o.getString("title"));
				m.setName(getResources().getString(R.string.administrator));
				m.setMessage(o.getString("message"));
				m.setTime(o.getString("time"));
				m.setVoice(o.getString("voice"));
				result.add(m);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private void startChat(final String sessionId) {
		try {
			appChat.setSessionId(sessionId);
			Stream textStream = new Stream(StreamType.Text, new StreamFormat("utf8"));
			appChat.setTextStream(textStream);
			receiveMessage();
			appChat.connect(new SingleAction<String>() {
				public void invoke(final String error) {
					
							if (error != null) {
								//update wait list
								if(!error.contains("detach-room")&&!error.contains("attach-room")){
									restartService();
								}
								
								
							}
						}
					
			}, myName,myIMEI);
			//Nhan request tu fragment client
			IntentFilter i = new IntentFilter("com.iii.smarthome.action.requestClient"); 
			receiver = new serviceReceiver(); 
			registerReceiver(receiver, i);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	public class serviceReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String requestClient = intent.getStringExtra("request");
			if(requestClient.contains(ConfigChannel.OFFER_VIDEO)){
				isVideoCall = isVideoComming = false;
			}else if(requestClient.contains(ConfigChannel.ANSWER_VIDEO)){
				isVideoCall = true;
				isVideoComming = false;
			}else if(requestClient.contains(ConfigChannel.NO_ANSWER)){
				isVideoCall = isVideoComming = false;
				appChat.sendMessage(Encrypt.getMD5(ConfigChannel.NO_ANSWER+ requestClient.replace(ConfigChannel.NO_ANSWER,"")));
			}else if(requestClient.contains(ConfigChannel.OFF_ANSWER)){
				isVideoCall = isVideoComming  = false;
				appChat.sendMessage(Encrypt.getMD5(ConfigChannel.OFF_ANSWER+ requestClient.replace(ConfigChannel.OFF_ANSWER,"")));
			}

		}
		
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	  private String getPeerName(BaseLinkArgs e)
	    {
	        @SuppressWarnings("unchecked")
			HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions.getPeerClient(e).getBoundRecords() : (HashMap<String, Record>)e.getPeerState());
	        try
	        {
	            return Serializer.deserializeString(peerBindings.get("name").getValueJson());
	        }
	        catch (Exception ex)
	        {
	            ex.printStackTrace();
	            return null;
	        }
	    }
		private String getPeerImei(BaseLinkArgs e) {
			@SuppressWarnings("unchecked")
			HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions.getPeerClient(e).getBoundRecords(): (HashMap<String, Record>) e.getPeerState());
			try {
				return Serializer.deserializeString(peerBindings.get("imei")
						.getValueJson());
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
}
