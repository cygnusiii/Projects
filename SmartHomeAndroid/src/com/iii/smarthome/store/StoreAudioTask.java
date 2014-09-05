package com.iii.smarthome.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import com.iii.smarthome.codec.SequenceEncoder;
import com.iii.smarthome.codec.WavHeader;

import fm.icelink.webrtc.AudioBuffer;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class StoreAudioTask extends AsyncTask<ArrayList<AudioBuffer>, String, Void> {
	private Context context;
	public StoreAudioTask(Context context){
		this.context=context;
	}
	@Override
	protected Void doInBackground(ArrayList<AudioBuffer>... params) {
		// TODO Auto-generated method stub
		final String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
		FileOutputStream fos  = null;
		try{
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/smarthome/audio");
			if(!dir.exists()){
				dir.mkdirs();
			}
			File audioFile = new File(dir.getAbsolutePath()+"/audio_"+timeStamp+".wav");
			fos = new FileOutputStream(audioFile,true);
			int count=0;
			WavHeader wavHeader = new WavHeader(36+params[0].size()*2*16/8,48000,192000,params[0].size()*2*16/8);
			fos.write(wavHeader.getHeader());
			fos.close();
			for(AudioBuffer ab:params[0]){
				count++;
				fos = new FileOutputStream(audioFile,true);
				fos.write(ab.getData());
				DecimalFormat df = new DecimalFormat("#.##");
				publishProgress(count+" of "+params[0].size());
				fos.close();
			}

		}catch(IOException ex){
			ex.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		Toast.makeText(context, "Save "+values[0], Toast.LENGTH_SHORT).show();
	}

}
