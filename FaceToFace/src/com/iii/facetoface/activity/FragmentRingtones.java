package com.iii.facetoface.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.iii.facetoface.R;
import com.iii.facetoface.activity.adapter.ListSqliteRingtonesAdapter;
import com.iii.facetoface.activity.adapter.ListStoreRingtonesAdapter;
import com.iii.facetoface.database.RingTonesDB;
import com.iii.facetoface.database.table.Ringtone;
import com.iii.facetoface.media.GetAudioByPath;
import com.iii.facetoface.webservice.ConfigServer;
import com.iii.facetoface.webservice.DataAccessHelper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class FragmentRingtones extends Fragment implements OnClickListener{
	private ArrayList<Ringtone> ringtones;
	private ListSqliteRingtonesAdapter adapterSqliteRingtones;
	private ListStoreRingtonesAdapter adapterStoreRingtones;
	private final String path = "/sdcard/MUSIC/";
	private RingTonesDB rDB;
	private ListView sqlite_ringtones,store_ringtones;
	private Button rt_add,rt_back,rt_del;

	private DataAccessHelper da= new DataAccessHelper(ConfigServer.WEBSERVICE);
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_ringtones, null);
		rDB = new RingTonesDB(getActivity());
		ringtones = rDB.getRingtones();
		adapterSqliteRingtones = new ListSqliteRingtonesAdapter(getActivity(),R.layout.sqlite_ringtones, ringtones);
		
		sqlite_ringtones = (ListView)view.findViewById(R.id.sqlite_ringtones);
		store_ringtones = (ListView)view.findViewById(R.id.store_ringtones);
		sqlite_ringtones.setAdapter(adapterSqliteRingtones);
		rt_add = (Button)view.findViewById(R.id.rt_add);
		rt_del = (Button)view.findViewById(R.id.rt_delete);
		rt_back = (Button)view.findViewById(R.id.rt_back);
		rt_add.setOnClickListener(this);
		rt_back.setOnClickListener(this);
		return view;
	}
	public ArrayList<Ringtone> getRingtonesByPath(){
		ArrayList<Ringtone> result = new ArrayList<Ringtone>();
		for(HashMap<String, String> song:new GetAudioByPath(path).getSongsList()){
			Iterator<String> keySetIterator = song.keySet().iterator();
			String name = "";
			String pathAbsolute="";
			while(keySetIterator.hasNext()){
			  String key = keySetIterator.next();
			  if(key.equals("songTitle"))name = song.get(key);
			  if(key.equals("songPath"))pathAbsolute = song.get(key);
			}
			 if(!name.isEmpty() && !pathAbsolute.isEmpty())result.add(new Ringtone(name, path));
		}
		return result;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.rt_add:
			sqlite_ringtones.setVisibility(View.GONE);
			store_ringtones.setVisibility(View.VISIBLE);
			rt_del.setVisibility(View.GONE);
			rt_back.setVisibility(View.VISIBLE);
			adapterStoreRingtones = new ListStoreRingtonesAdapter(getActivity(),R.layout.store_ringtones, getRingtonesByPath());
			store_ringtones.setAdapter(adapterStoreRingtones);
			break;
		case R.id.rt_back:
			sqlite_ringtones.setVisibility(View.VISIBLE);
			store_ringtones.setVisibility(View.GONE);
			rt_del.setVisibility(View.VISIBLE);
			rt_back.setVisibility(View.GONE);
			ringtones.clear();
			for(Ringtone ringtone:rDB.getRingtones()){
				ringtones.add(ringtone);
				adapterSqliteRingtones.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}		
	}

}
