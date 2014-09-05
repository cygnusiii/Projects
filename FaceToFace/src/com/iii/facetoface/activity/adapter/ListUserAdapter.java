package com.iii.facetoface.activity.adapter;

import java.util.ArrayList;

import com.iii.facetoface.R;
import com.iii.facetoface.database.table.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.TextView;

public class ListUserAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<User> mUsers;
	public ListUserAdapter(Context context, ArrayList<User> users) {
		super();
		this.mContext = context;
		this.mUsers = users;
	}
	@Override
	public int getCount() {
		return mUsers.size();
	}
	@Override
	public Object getItem(int position) {		
		return mUsers.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		User user  = (User)this.getItem(position);
		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.user_row, parent, false);
			holder.name = (TextView) convertView.findViewById(R.id.user_name);
			holder.number = (TextView) convertView.findViewById(R.id.user_number);	
			convertView.setTag(holder);
		}
		else holder = (ViewHolder) convertView.getTag();
		
		holder.name.setText(user.getName());
		holder.number.setText(user.getNumber());

		return convertView;
	}
	private static class ViewHolder
	{
		TextView name;
		TextView number;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
