package com.iii.facetoface.media;

import com.iii.facetoface.activity.ClientActivity;
import com.iii.facetoface.activity.VideoActivity;
import com.iii.facetoface.conference.AppChat;
import com.iii.facetoface.webservice.ConfigChannel;
import com.iii.facetoface.webservice.Encrypt;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;

public class AudioPlayer {
	private String location;
	private MediaPlayer mp;
	private Context context;
	public AudioPlayer(String location){
		this.location = location;
		this.mp = new MediaPlayer();
	}
	public AudioPlayer() {
		// TODO Auto-generated constructor stub
	}
	public Boolean isPlay(){
		return mp.isPlaying();
	}
	public void start(Context context,int id,int time,String offerIMEI){
	    try {
	    this.context = context;
		mp = MediaPlayer.create(context, id);
		mp.start();
		mp.setLooping(true);
		stopByTime(time,offerIMEI);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void start(int time,String offerIMEI){
	    //set up MediaPlayer    
	    try {
	        mp.setDataSource(location);
	        mp.prepare();
	        mp.start();
	        mp.setLooping(true);
	        stopByTime(time,offerIMEI);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void stop(){
		mp.stop();
	}
	public void stopByTime(int time,final String offerIMEI){
		Handler h = new Handler();
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mp.stop();
				try {
					((VideoActivity)context).stopVideoIncomming();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//mp.release();
			}
		};
		h.postDelayed(r, time*1000);
	}
	
}
