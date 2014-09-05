package com.iii.smarthome.activity;

import com.iii.smarthome.R;
import com.iii.smarthome.database.table.Message;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragmentMessageInfo extends Fragment {
	private Message message;
	private TextView info_offer_name,info_offer_title,info_offer_message;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_message, container,false);
		Bundle bundle = getArguments();
		message = (Message)bundle.getSerializable("message");
		info_offer_name = (TextView)view.findViewById(R.id.info_offer_name);
		info_offer_title= (TextView)view.findViewById(R.id.info_offer_title);
		info_offer_message = (TextView)view.findViewById(R.id.info_offer_message);
		info_offer_name.setText(message.getName()+" - "+message.getTime());
		info_offer_message.setText(message.getMessage());
		info_offer_title.setText(message.getTitle());
		return view;
	}

}
