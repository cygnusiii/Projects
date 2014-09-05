package com.iii.smarthome.activity;

import java.util.ArrayList;

import com.iii.smarthome.activity.adapter.ListMessageAdapter;
import com.iii.smarthome.database.table.Message;
import com.iii.smarthome.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FragmentLog extends Fragment {
	private ArrayList<Message> messages;
	private ListView list_msg;
	private ListMessageAdapter adapter_msg;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_log, container,false);
		return view;
	}

}
