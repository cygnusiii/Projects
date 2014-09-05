package com.iii.facetoface.activity;

import java.util.ArrayList;

import com.iii.facetoface.R;
import com.iii.facetoface.conference.AppVideo;
import com.iii.facetoface.database.RingTonesDB;
import com.iii.facetoface.media.AudioPlayer;
import com.iii.facetoface.webservice.ConfigChannel;
import com.iii.facetoface.webservice.Encrypt;

import fm.SingleAction;
import fm.icelink.webrtc.DefaultProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class VideoActivity extends Activity implements OnClickListener{
	private String offerIMEI,myIMEI,offerName,myName,myNumber,offerNumber;
	private AudioPlayer ap;
	private RingTonesDB rDB; 
	private AppVideo appVideo;
	private String sessionId;
	private GestureDetector gestureDetector;
	private Animation animation ;
	private BroadcastReceiver receiver;
	private static RelativeLayout container_video;
	private LinearLayout layout,info_video;
	private TextView info_user;
	private ImageButton bt_video,bt_voice,bt_cancel;
	private boolean isCallStarted,isComming,isVideoStarted,isVoiceStarted;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	
		setContentView(R.layout.activity_video);
		//
		Bundle bundle = getIntent().getExtras();
		myName = bundle.getString("myName");myIMEI = bundle.getString("myIMEI");myNumber = bundle.getString("myNumber");
		offerName = bundle.getString("offerName");offerIMEI = bundle.getString("offerIMEI");offerNumber = bundle.getString("offerNumber");
		isComming = bundle.getBoolean("isComming");
		//
		animation= new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
	    animation.setDuration(1000);
	    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
	    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
	    animation.setRepeatMode(Animation.REVERSE); // 
	    
	    layout = (LinearLayout)findViewById(R.id.layout_video);info_video = (LinearLayout)findViewById(R.id.info_video);
	    info_user = (TextView)findViewById(R.id.info_user);
	    bt_video = (ImageButton)findViewById(R.id.bt_call_video);bt_video.setOnClickListener(this);
	    bt_cancel = (ImageButton)findViewById(R.id.bt_call_cancel);bt_cancel.setOnClickListener(this);
	    bt_voice = (ImageButton)findViewById(R.id.bt_call_voice);bt_voice.setOnClickListener(this);

		try {
			appVideo = AppVideo.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// register & define filter for local listener
		IntentFilter i = new IntentFilter("com.iii.facetoface.action.responseClient"); 
		receiver = new reponseReceiver(); 
		registerReceiver(receiver, i);
		
		if (isComming) {
			info_user.setText(offerName+" Đang gọi ...");
			bt_video.setAnimation(animation);
			rDB = new RingTonesDB(this);
			if (!rDB.getRingType()) {
				ap = new AudioPlayer();
				ap.start(this, R.raw.nokia, 10, offerIMEI);
			} else {
				ap = new AudioPlayer(rDB.getLocation());
				ap.start(10, offerIMEI);
			}
		}
	    else {
	    	isVoiceStarted = isVideoStarted = isCallStarted = true;
			String sessionId = Encrypt.getMD5(ConfigChannel.PREFIX_VIDEO + this.offerIMEI + this.myIMEI);
			startVideo(sessionId);
	    }
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			appVideo.disconnect(new SingleAction<String>(){
				public void invoke(String error){
					if(error!=null)alert(error);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unregisterReceiver(receiver);
	}
	public class reponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			final String serviceData = intent.getStringExtra("serviceData");
			if(serviceData.contains(ConfigChannel.ANSWER_BUSY)){
				setOfferIMEI(intent.getStringExtra("offerIMEI"));
				information(serviceData.replace(ConfigChannel.ANSWER_BUSY, "") + " Đang bận !");
				stopVideo();
			}else if(serviceData.contains(ConfigChannel.NO_ANSWER)){
				setOfferIMEI(intent.getStringExtra("offerIMEI"));
				information(serviceData.replace(ConfigChannel.NO_ANSWER, "") + " Không nghe !");
				stopVideo();
			}else if(serviceData.contains(ConfigChannel.OFF_ANSWER)){
				setOfferIMEI(intent.getStringExtra("offerIMEI"));
				information(serviceData.replace(ConfigChannel.OFF_ANSWER, "") + " Kết thúc cuộc gọi !");
				stopVideo();
			} 		
		}
	}
	public void stopVideoIncomming(){
		if(!isCallStarted){
			stopVideo();
			requestService(ConfigChannel.NO_ANSWER+offerIMEI);
		}
		
		
	}
	public void stopVideo(){
		runOnUiThread(new Runnable() {
			public void run() {
				
				try {
					appVideo.disconnect(new SingleAction<String>(){
						public void invoke(String error){
							if(error!=null)alert(error);
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (ap!=null && ap.isPlay())
					ap.stop();
				finish();
			}
		});
	}
	private void startVideo(String sessionId) {
		try {
			DefaultProviders.setAndroidContext(this);
			container_video = (RelativeLayout)findViewById(R.id.container_video);
			container_video.setVisibility(View.VISIBLE);
			info_video.setVisibility(View.GONE);
			//layout.removeView(container_video);
			Toast.makeText(this, "Double-tap to switch camera.",Toast.LENGTH_SHORT).show();
			gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() {
				public boolean onDoubleTap(MotionEvent e) {
					appVideo.useNextVideoDevice();
					return true;
				}
			});
			appVideo.setSessionId(sessionId);
			appVideo.connect(new SingleAction<String>(){
				public void invoke(String error){
					if(error!=null){
						alert(error);
					}
				}
			}, container_video);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	private void acceptCall(){
		bt_video.clearAnimation();bt_voice.clearAnimation();
		String sessionId = Encrypt.getMD5(ConfigChannel.PREFIX_VIDEO + this.myIMEI+ this.offerIMEI);
		startVideo(sessionId);
	}
	public void setOfferIMEI(String offerIMEI){
		this.offerIMEI = offerIMEI;
	}
	public void information(String format,Object... args){
		final String text = String.format(format, args);
		final Activity self = this;
		self.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(self, text,Toast.LENGTH_SHORT).show();
			}
		});
	}
	public void alert(String format, Object... args) {
		final String text = String.format(format, args);
		final Activity self = this;
		self.runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog.Builder alert = new AlertDialog.Builder(self);
				alert.setMessage(text);
				alert.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				alert.show();
			}
		});
	}
	public void requestService(String request){
		Intent intent = new Intent("com.iii.facetoface.action.requestClient");
		intent.putExtra("request", request);
		sendBroadcast(intent);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

			switch (v.getId()) {
			case R.id.bt_call_video:
				if (appVideo.localMediaStarted()) {
					if(isVideoStarted){
						appVideo.muteLocalVideo();isVideoStarted = false;
						bt_video.setBackground(getResources().getDrawable(R.drawable.button_red));
					}
					else {
						appVideo.unMuteLocalVoice();isVideoStarted = true;
						bt_video.setBackground(getResources().getDrawable(R.drawable.button_green));
					}
				}
				if(isComming&&!isCallStarted){
					isCallStarted = true;
					acceptCall();
					if (ap!=null && ap.isPlay())ap.stop();
					requestService(ConfigChannel.ANSWER_VIDEO);
				}
				
				break;
			case R.id.bt_call_voice:
				if (appVideo.localMediaStarted()) {
					if(isVoiceStarted){
						appVideo.pauseLocalVoice();isVoiceStarted = false;
						bt_voice.setBackground(getResources().getDrawable(R.drawable.button_red));
					}
					else {
						appVideo.resumeLocalVoice();isVoiceStarted = true;
						bt_voice.setBackground(getResources().getDrawable(R.drawable.button_green));
					}
				}
				if(isComming&&!isCallStarted){
					isCallStarted = true;
					acceptCall();
					if (ap!=null && ap.isPlay())ap.stop();
					requestService(ConfigChannel.ANSWER_VIDEO);
				}
				
				break;
			case R.id.bt_call_cancel:
				if (ap!=null && ap.isPlay())ap.stop();
				if(isCallStarted||!isComming)requestService(ConfigChannel.OFF_ANSWER+offerIMEI);
				else requestService(ConfigChannel.NO_ANSWER+offerIMEI);
				finish();
				break;
			default:
				break;
			}	
	}
	protected void onResume() {
		super.onResume();
		if (appVideo.localMediaStarted()) {
			appVideo.resumeLocalVideo();
		}

	}

	protected void onPause() {
		if (appVideo.localMediaStarted()) {
			appVideo.pauseLocalVideo();
		}
		super.onPause();

	}
	
	@Override
	public void onBackPressed() {
		if (appVideo.localMediaStarted()) {
			stopVideo();
		}
		super.onBackPressed();
	}
}
