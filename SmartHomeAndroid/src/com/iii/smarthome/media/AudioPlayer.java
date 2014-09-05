package com.iii.smarthome.media;

import com.iii.smarthome.activity.VideoActivity;

import android.content.Context;
import android.media.AudioManager;
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
	public void start(Context context,int id,int time){
	    try {
	    this.context = context;
		mp = MediaPlayer.create(context, id);
		mp.start();
		mp.setLooping(true);
		stopByTime(time);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void start(int time){
	    //set up MediaPlayer    
	    try {
	        mp.setDataSource(location);
	        mp.prepare();
	        mp.start();
	        mp.setLooping(true);
	        stopByTime(time);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void start(){
		try {
	        mp.setDataSource(location);
	        mp.prepare();
	        mp.start();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void startStream(){
		try {
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mp.setDataSource(location);
	        mp.prepare();
	        mp.start();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	public void stop(){
		mp.stop();
	}
	public void stopByTime(int time){
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
