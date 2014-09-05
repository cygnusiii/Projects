package com.iii.smarthome.activity.adapter;

import java.util.ArrayList;

import com.iii.smarthome.R;
import com.iii.smarthome.database.table.Message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListMessageAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<Message> mMessages;
	public ListMessageAdapter(Context context, ArrayList<Message> messages) {
		super();
		this.mContext = context;
		this.mMessages = messages;
	}
	@Override
	public int getCount() {
		return mMessages.size();
	}
	@Override
	public Object getItem(int position) {		
		return mMessages.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Message message = (Message) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.message_row, parent, false);
			holder.offer_name = (TextView) convertView.findViewById(R.id.offer_name);
			holder.offer_time = (TextView) convertView.findViewById(R.id.offer_time);
			
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		holder.offer_name.setText(message.getName());
		holder.offer_time.setText(message.getTime());
		
		return convertView;
	}
	private static class ViewHolder
	{
		TextView offer_name,offer_time;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
