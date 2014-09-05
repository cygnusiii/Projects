package com.iii.smarthome.store;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.iii.smarthome.activity.ClientActivity;
import com.iii.smarthome.codec.SequenceEncoder;
import com.iii.smarthome.webservice.ConfigServer;
import com.iii.smarthome.webservice.DataAccessHelper;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class StoreVideoTask extends AsyncTask<ArrayList<YuvImage>,String,Void> {
	private Context context;
	private String startTime;
	public StoreVideoTask(Context context,String startTime){
		this.context=context;
		this.startTime = startTime;
	}
	@Override
	protected Void doInBackground(ArrayList<YuvImage>... params) {
		// TODO Auto-generated method stub
		final String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.UK).format(new Date());
		try{
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/smarthome/video");
			if(!dir.exists()){
				dir.mkdirs();
			}
			String fileName = dir.getAbsolutePath() + "/video_" + timeStamp+ ".mp4";
			SequenceEncoder	se = new SequenceEncoder(new File(fileName));
			int count=0;
			for(YuvImage yuv:params[0]){
				count++;
				Bitmap bm = se.getBitmapFromYUV(yuv);
				se.encodeImage(bm);
				if(bm!=null)bm.recycle();
				bm=null;
				DecimalFormat df = new DecimalFormat("#.##");
				publishProgress(df.format((count*1.0/params[0].size())*100)+"--"+params[0].size());
			}
			se.finish();
			
			UploadFileTask upFile = new UploadFileTask(context);
			upFile.execute(fileName);
			while(!upFile.isStatus());
			if(upFile.isStatus()){
				JSONObject json  = new JSONObject();
				try {
					json.accumulate("myIMEI", ((ClientActivity)context).getIMEI()[0]);
					json.accumulate("offerIMEI", ((ClientActivity)context).getIMEI()[1]);
					json.accumulate("endTime", timeStamp);
					json.accumulate("startTime", startTime);
					json.accumulate("captureName","pic_" + startTime+ ".jpg");
					json.accumulate("videoName","video_" + timeStamp+ ".mp4");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new DataAccessHelper(ConfigServer.WEBSERVICE).responseString("conference",json.toString());
				
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
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		Toast.makeText(context, "Save "+values[0]+" %", Toast.LENGTH_SHORT).show();
	}
}
