package com.iii.facetoface.activity.adapter;

import java.util.ArrayList;

import com.iii.facetoface.R;
import com.iii.facetoface.database.table.Message;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
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
			convertView = LayoutInflater.from(mContext).inflate(R.layout.msg_row, parent, false);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		
		holder.message.setText(message.getMessage());
		
		LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
		if(message.isStatusMessage())
		{
			holder.message.setBackgroundDrawable(null);
			lp.gravity = Gravity.LEFT;
			holder.message.setTextColor(R.color.textFieldColor);
		}
		else
		{		
			if(message.isMine())
			{
				holder.message.setBackgroundResource(R.drawable.msg_out);
				lp.gravity = Gravity.RIGHT;
			}
			else
			{
				holder.message.setBackgroundResource(R.drawable.msg_in);
				lp.gravity = Gravity.LEFT;
			}
			holder.message.setLayoutParams(lp);
			holder.message.setTextColor(R.color.textColor);	
		}
		return convertView;
	}
	private static class ViewHolder
	{
		TextView message;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
