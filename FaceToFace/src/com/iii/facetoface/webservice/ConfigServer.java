package com.iii.facetoface.webservice;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigServer {
	// public static final String websyncRequestUrl =
	// "http://117.6.131.222:8181/websync.ashx";
	public static String websyncRequestUrl = "";
	public static String icelinkServerAddress = "";
	public static final String WEBSERVICE = "http://117.6.131.222:6789/smarthomews/";
	public static final String version = "PHIÊN BẢN 2.1";
	private static DataAccessHelper da;

	public static void serverConfig() {
		da = new DataAccessHelper(WEBSERVICE);
		JSONArray jServers = null;
		String result = "";

		try {
			result = da.responseString("server_config", "ok");
			JSONObject json = new JSONObject(result);
			jServers = json.getJSONArray("Servers");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jServers != null) {
			if (jServers.length() == 1) {

				try {
					icelinkServerAddress = jServers.getJSONObject(0).getString("icelinkServer");
					websyncRequestUrl = "http://"+ jServers.getJSONObject(0).getString("websyncServer") + "/websync.ashx";
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}
