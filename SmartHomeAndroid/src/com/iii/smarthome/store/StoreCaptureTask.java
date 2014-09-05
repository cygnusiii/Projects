package com.iii.smarthome.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;
import com.iii.smarthome.webservice.ConfigServer;
import com.iii.smarthome.webservice.DataAccessHelper;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class StoreCaptureTask extends AsyncTask<YuvImage, Void, Void> {
	private Context context;
	private String timeStamp;
	private Boolean status = false;
	public StoreCaptureTask(Context context){
		this.context=context;
	}
	@Override
	protected Void doInBackground(YuvImage... params) {
		// TODO Auto-generated method stub
		timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss",
				Locale.UK).format(new Date());
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/smarthome/capture");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = dir.getAbsolutePath() + "/pic_" + timeStamp+ ".jpg";
		File f = new File(fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			params[0].compressToJpeg(new Rect(0, 0, params[0].getWidth(), params[0].getHeight()), 100, fos);
			fos.flush();
			fos.close();
			UploadFileTask uft  = new UploadFileTask(context);
			uft.execute(fileName);
			if(uft.isStatus())this.status = true;

		} catch (FileNotFoundException e) {
			e.getStackTrace();
		} catch (IOException e) {
			e.getStackTrace();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Toast.makeText(context, "Captured!", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
	public String getTime(){
		return this.timeStamp;
	}
	public Boolean isStatus(){
		return this.status;
	}

}
