package com.iii.smarthome.activity.adapter;

import java.util.ArrayList;

import com.iii.smarthome.R;
import com.iii.smarthome.database.table.Ringtone;
import com.iii.smarthome.media.AudioPlayer;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListStoreRingtonesAdapter extends BaseAdapter{
	private Context mContext;
	private AudioPlayer ap;
	private ArrayList<Ringtone> mRingtones;
	private Handler handler = new Handler();
	public ListStoreRingtonesAdapter(Context context, ArrayList<Ringtone> ringtones) {
		super();
		this.mContext = context;
		this.mRingtones = ringtones;
	}
	@Override
	public int getCount() {
		return mRingtones.size();
	}
	@Override
	public Object getItem(int position) {		
		return mRingtones.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Ringtone ringtone = (Ringtone) this.getItem(position);

		final ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.store_ringtones, parent, false);
			holder.name = (TextView)convertView.findViewById(R.id.rt_store_name);	
			holder.image = (ImageView)convertView.findViewById(R.id.rt_store_image);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		holder.name.setText(ringtone.getName());
		holder.image.setImageResource(R.drawable.play);
		final ListStoreRingtonesAdapter self = this;
		holder.image.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!ringtone.isPlay()){
					if(ap!=null&&ap.isPlay()){ap.stop();self.notifyDataSetChanged();}
					holder.image.setImageResource(R.drawable.pause);
					ringtone.setPlay(true);
					ap = new AudioPlayer(ringtone.getPath()+ringtone.getName()+".mp3");
					ap.start();
					new Thread(){
						public void run(){
							while(ap!=null&&ap.isPlay());
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									holder.image.setImageResource(R.drawable.play);
									ringtone.setPlay(false);
									if(ap!=null&&ap.isPlay())ap.stop();
								}
							});
							
						}
					}.start();
				}
				else{
					holder.image.setImageResource(R.drawable.play);
					ringtone.setPlay(false);
					if(ap!=null&&ap.isPlay())ap.stop();
				}
				
			}
		});
		
		return convertView;
	}
	public void stopRingTone(){
		if(ap!=null&&ap.isPlay())ap.stop();
	}
	private static class ViewHolder
	{
		TextView name;ImageView image;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	

}
