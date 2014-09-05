package com.iii.facetoface.receiver;

import com.iii.facetoface.service.ClientService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClientBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
			Intent startServiceIntent = new Intent(context, ClientService.class);
			context.startService(startServiceIntent);
		}
	}

}
