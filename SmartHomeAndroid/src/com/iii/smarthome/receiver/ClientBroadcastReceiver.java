package com.iii.smarthome.receiver;

import com.iii.smarthome.R;
import com.iii.smarthome.activity.VideoActivity;
import com.iii.smarthome.media.AudioPlayer;
import com.iii.smarthome.service.ClientService;

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
		} else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_POWER_DISCONNECTED)) {
			new AudioPlayer().start(context, R.raw.ringtone, 10);
			Intent i = new Intent(context, VideoActivity.class);
			i.putExtra("myName", "0");
			i.putExtra("myIMEI", "0" );
			i.putExtra("offerName", "0");
			i.putExtra("offerIMEI","0");
			i.putExtra("isComming", false);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

}