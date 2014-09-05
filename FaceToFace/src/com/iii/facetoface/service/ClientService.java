package com.iii.facetoface.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.iii.facetoface.R;
import com.iii.facetoface.activity.ClientActivity;
import com.iii.facetoface.activity.VideoActivity;
import com.iii.facetoface.conference.AppChat;
import com.iii.facetoface.database.LoginDB;
import com.iii.facetoface.database.table.User;
import com.iii.facetoface.database.table.Message;
import com.iii.facetoface.webservice.ConfigChannel;
import com.iii.facetoface.webservice.ConfigServer;
import com.iii.facetoface.webservice.Encrypt;

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
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

public class ClientService extends Service {
	private boolean useWebSyncExtension = true;
	private ArrayList<Message> messages;
	private HashMap<User, ArrayList<Message>> hashMessage;
	private AppChat appChat;
	private LoginDB lgDB;
	private BroadcastReceiver receiver;
	private String myName,myIMEI,myNumber;
	private ArrayList<String> users = new ArrayList<String>();
	private boolean isVideoCall,isVideoComming,isVideoGoing;
	private NotificationManager NM;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		try {
			Toast.makeText(this, "FaceToFace service started ...", Toast.LENGTH_LONG).show();
			lgDB = new LoginDB(getBaseContext());
			
			if (lgDB.getNumRow() == 1) {
				ArrayList<User> users = lgDB.getUser();
				this.myName = users.get(0).getName();
				this.myIMEI =  users.get(0).getIMEI();
				this.myNumber = users.get(0).getNumber();
				ConfigServer.serverConfig();
				appChat = AppChat.getInstance();
				startChat(ConfigChannel.PUBLIC_CHANNEL);
				hashMessage = new HashMap<User, ArrayList<Message>>();
				messages = new ArrayList<Message>();
			}else{
				this.stopSelf();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiver);
		Toast.makeText(this, "FaceToFace service stopped ...", Toast.LENGTH_LONG).show();
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

	@SuppressWarnings("deprecation")
	public void notifyMessages(User u, Message msg) {
		String title = "Tin nháº¯n tá»« " + u.getName();
		String subject = u.getName();
		String body = msg.getMessage();
		NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notify = new Notification(R.drawable.avatar, title,System.currentTimeMillis());
		PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(ClientService.this,ClientActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
		notify.setLatestEventInfo(getApplicationContext(), subject, body,pending);
		notify.sound = Uri.parse("android.resource://"+ getPackageName() + "/" +R.raw.message);
		notify.vibrate = new long[]{0,100,200,300};
		NM.notify(0, notify);
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
				final String offerNumber = getPeerNumber(e);
				//Nháº­n thÃ´ng bÃ¡o busy tá»« answer
				if (Encrypt.Compare(offerMessage, ConfigChannel.ANSWER_BUSY+ myIMEI)||(isVideoComming&&isVideoGoing)) {
					if(isVideoGoing&&isVideoComming){
						appChat.sendMessage(Encrypt.getMD5(ConfigChannel.ANSWER_BUSY+ offerIMEI));
					}
					isVideoCall = isVideoComming = isVideoGoing= false;
					//gui broadcast cho fragmentclient
					if(isClientTop(VideoActivity.class)){
						Intent intent = new Intent("com.iii.facetoface.action.responseClient");
						intent.putExtra("serviceData",ConfigChannel.ANSWER_BUSY+offerName); 
						sendBroadcast(intent);
						
					}
					
				}
				//NhÃ¢n thÃ´ng bÃ¡o tá»« chá»‘i tá»« answer
				if (Encrypt.Compare(offerMessage, ConfigChannel.NO_ANSWER+ myIMEI)) {
					isVideoCall = isVideoComming = isVideoGoing= false;
					//gui broadcast cho fragmentclient
					if(isClientTop(VideoActivity.class)){
						Intent intent = new Intent("com.iii.facetoface.action.responseClient");
						intent.putExtra("serviceData",ConfigChannel.NO_ANSWER+offerName); 
						sendBroadcast(intent);
					}				
				}
				//Ngáº¯t cuá»™c gá»�i tá»« answer
				if (Encrypt.Compare(offerMessage, ConfigChannel.OFF_ANSWER+ myIMEI)) {
					isVideoCall = isVideoComming = isVideoGoing= false;
					//gui broadcast cho fragmentclient
					if(isClientTop(VideoActivity.class)){
						Intent intent = new Intent("com.iii.facetoface.action.responseClient");
						intent.putExtra("serviceData",ConfigChannel.OFF_ANSWER+offerName); 
						sendBroadcast(intent);
					}
					
				}
				if (Encrypt.Compare(offerMessage, ConfigChannel.OFFER_VIDEO+ myIMEI)) {
					if(!isVideoCall&&!isVideoComming&& !isVideoGoing){
						isVideoComming = true;
						if(!isClientTop(VideoActivity.class)){
							//goi activity client 
							 Intent i = new Intent(ClientService.this, VideoActivity.class);
							 i.putExtra("myName", myName);
							 i.putExtra("myIMEI", myIMEI);
							 i.putExtra("myNumber", myNumber);
							 i.putExtra("offerName", offerName);
							 i.putExtra("offerIMEI", offerIMEI);
							 i.putExtra("offerNumber", offerNumber);
							 i.putExtra("isComming",true);
							 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							 startActivity(i);
						}
//						//gui broadcast cho fragmentclient
//						Intent intent = new Intent("com.iii.facetoface.action.responseClient");
//						intent.putExtra("serviceData",ConfigChannel.OFFER_VIDEO+offerName); 
//						intent.putExtra("offerIMEI",offerIMEI);
//						sendBroadcast(intent);
					

					}else{
						//Ä‘ang conference vá»›i ngÆ°á»�i khÃ¡c 
						appChat.sendMessage( Encrypt.getMD5(ConfigChannel.ANSWER_BUSY+ offerIMEI));
					}
				}
				///private chat
				if(offerMessage.contains(Encrypt.getMD5(ConfigChannel.PREFIX_CHAT + myIMEI))){
					//update new messages
					User u = new User(offerName, offerNumber, offerIMEI);
					String message = offerMessage.replace(Encrypt.getMD5(ConfigChannel.PREFIX_CHAT + myIMEI), "");
					if(hashMessage.get(u)!=null){
						hashMessage.get(u).add(new Message(message, false));
							
					}
					else {
						//put msg for new user
						ArrayList<Message> temp  = new ArrayList<Message>();
						temp.add(new Message(message, false));
						hashMessage.put(u, temp);
					}
					if(isClientTop(ClientActivity.class)){
						Bundle b = new Bundle();
						b.putString("serviceData",offerMessage);
						b.putSerializable("offerUser",new User(offerName, offerNumber, offerIMEI));
						Intent intent = new Intent("com.iii.facetoface.action.responseClient");
						intent.putExtras(b); 
						sendBroadcast(intent);
					}else{
						//display in notify
						notifyMessages(u, new Message(message, false));
					}
					
					
				}
					
			}
		});
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
								if(error.contains("attach-room")){
									
									if(!users.contains(error.replace("attach-room", ""))){
										users.add(error.replace("attach-room", ""));
										if(isClientTop(ClientActivity.class)){
											Intent intent = new Intent("com.iii.facetoface.action.responseClient");
											intent.putExtra("serviceData",error); 
											sendBroadcast(intent);
											
										}
									}
									
									
								}
								else if(error.contains("detach-room")){
									users.remove(error.replace("detach-room", ""));
									if(isClientTop(ClientActivity.class)){
										Intent intent = new Intent("com.iii.facetoface.action.responseClient");
										intent.putExtra("serviceData",error); 
										sendBroadcast(intent);
										
									}
								}else restartService();
								
								
							}
						}
					
			}, myName,myIMEI,myNumber);
			//Nhan request tu fragment client
			IntentFilter i = new IntentFilter("com.iii.facetoface.action.requestClient"); 
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
			if(requestClient.equals("users")){
				for(String user:users){
					Intent i = new Intent("com.iii.facetoface.action.responseClient");
					i.putExtra("serviceData","attach-room" + user); 
					sendBroadcast(i);
				}
			}
			else if(requestClient.equals("messages")){
				Intent i = new Intent("com.iii.facetoface.action.responseClient");
				i.putExtra("serviceData","receive-messages"); 
				i.putExtra("hashMessage", hashMessage);
				sendBroadcast(i);
			}else if(requestClient.contains(ConfigChannel.OFFER_VIDEO)){
				isVideoGoing = true;
				isVideoCall = isVideoComming = false;
			}else if(requestClient.contains(ConfigChannel.ANSWER_VIDEO)){
				isVideoCall = true;
				isVideoComming = isVideoGoing = false;
			}else if(requestClient.contains(ConfigChannel.NO_ANSWER)){
				isVideoCall = isVideoComming = isVideoGoing = false;
				appChat.sendMessage(Encrypt.getMD5(ConfigChannel.NO_ANSWER+ requestClient.replace(ConfigChannel.NO_ANSWER,"")));
			}else if(requestClient.contains(ConfigChannel.OFF_ANSWER)){
				isVideoCall = isVideoComming = isVideoGoing = false;
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
		private String getPeerNumber(BaseLinkArgs e) {
			@SuppressWarnings("unchecked")
			HashMap<String, Record> peerBindings = (useWebSyncExtension ? BaseLinkArgsExtensions.getPeerClient(e).getBoundRecords(): (HashMap<String, Record>) e.getPeerState());
			try {
				return Serializer.deserializeString(peerBindings.get("number")
						.getValueJson());
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
}
