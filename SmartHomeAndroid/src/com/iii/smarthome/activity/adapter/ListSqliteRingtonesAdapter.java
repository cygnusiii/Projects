package com.iii.smarthome.activity.adapter;

import java.util.ArrayList;

import com.iii.smarthome.R;
import com.iii.smarthome.database.RingTonesDB;
import com.iii.smarthome.database.table.Ringtone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ListSqliteRingtonesAdapter extends BaseAdapter{
	private Context mContext;
	private RingTonesDB dB ;
	private ArrayList<Ringtone> mRingtones;
	public ListSqliteRingtonesAdapter(Context context, ArrayList<Ringtone> ringtones) {
		super();
		this.mContext = context;
		this.mRingtones = ringtones;
		this.dB = new RingTonesDB(context);
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

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.sqlite_ringtones, parent, false);
			holder.name = (TextView)convertView.findViewById(R.id.rt_sqlite_name);	
			holder.image = (ImageView)convertView.findViewById(R.id.rt_sqlite_image);
			holder.time = (Spinner)convertView.findViewById(R.id.rt_sqlite_time);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		holder.name.setText(ringtone.getName());
		holder.image.setImageResource(mContext.getResources().obtainTypedArray(R.array.ringtone_type_image).getResourceId(ringtone.getType()-1, -1));
		
		ArrayAdapter adapter = ArrayAdapter.createFromResource(mContext, R.array.ringtone_times, android.R.layout.simple_list_item_1);
		holder.time.setAdapter(adapter);
		holder.time.setSelection(adapter.getPosition(String.valueOf(ringtone.getTime())));
		holder.time.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				ringtone.setTime(Integer.valueOf(arg0.getItemAtPosition(arg2).toString()));
				dB.insertRingtone(ringtone);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		return convertView;
	}
	private static class ViewHolder
	{
		TextView name;ImageView image;Spinner time;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	

}
