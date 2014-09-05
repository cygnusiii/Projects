package com.iii.smarthome.activity.adapter;

import java.util.ArrayList;

import com.iii.smarthome.R;
import com.iii.smarthome.database.table.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListSettingAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<Settings> mSettings;
	public ListSettingAdapter(Context context, ArrayList<Settings> settings) {
		super();
		this.mContext = context;
		this.mSettings = settings;
	}
	@Override
	public int getCount() {
		return mSettings.size();
	}
	@Override
	public Object getItem(int position) {		
		return mSettings.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Settings setting = (Settings) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.setting_row, parent, false);
			holder.name = (TextView) convertView.findViewById(R.id.setting_name);
			
			holder.image = (ImageView)convertView.findViewById(R.id.setting_image);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		holder.image.setImageResource(setting.getResource());
		holder.name.setText(setting.getName());
		
		return convertView;
	}
	private static class ViewHolder
	{
		TextView name;
		ImageView image;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
