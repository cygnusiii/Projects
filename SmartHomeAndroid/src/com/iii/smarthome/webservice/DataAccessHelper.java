package com.iii.smarthome.webservice;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.StrictMode;

public class DataAccessHelper {
	private String TAG_URL;
	public DataAccessHelper(String url){
		this.TAG_URL = url;
	}
	public String responseString(String type,String data){
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
		String str = "";
		HttpResponse response;
        HttpClient myClient = new DefaultHttpClient();
        HttpPost myConnection = new HttpPost(TAG_URL);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair(type,data));
        
        try {
        	myConnection.setEntity(new UrlEncodedFormEntity(pairs));
        	response = myClient.execute(myConnection);
            str = EntityUtils.toString(response.getEntity(), "UTF-8");
            
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
        return str;
	}
	public String responseString(String[] types,String[] datas){
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
		String str="";
		HttpResponse response;
        HttpClient myClient = new DefaultHttpClient();
        HttpPost myConnection = new HttpPost(TAG_URL);
        myConnection.setHeader("Content-Type", "application/x-www-form-urlencoded");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for(int i=0;i<types.length;i++){
        	pairs.add(new BasicNameValuePair(types[i],datas[i]));
        }
        
        
        try {
        	myConnection.setEntity(new UrlEncodedFormEntity(pairs));
        	response = myClient.execute(myConnection);
            str = EntityUtils.toString(response.getEntity(), "UTF-8");
            
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
		return str;
	}
}
