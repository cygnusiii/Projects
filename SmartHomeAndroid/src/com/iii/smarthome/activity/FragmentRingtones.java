package com.iii.smarthome.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.iii.smarthome.R;
import com.iii.smarthome.activity.adapter.ListSqliteRingtonesAdapter;
import com.iii.smarthome.activity.adapter.ListStoreRingtonesAdapter;
import com.iii.smarthome.activity.adapter.SwipeDismissListViewTouchListener;
import com.iii.smarthome.database.RingTonesDB;
import com.iii.smarthome.database.table.Ringtone;
import com.iii.smarthome.media.GetAudioByPath;
import com.iii.smarthome.webservice.ConfigServer;
import com.iii.smarthome.webservice.DataAccessHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentRingtones extends Fragment implements OnClickListener{
	private ArrayList<Ringtone> ringtones;
	private ListSqliteRingtonesAdapter adapterSqliteRingtones;
	private ListStoreRingtonesAdapter adapterStoreRingtones;
	private final String path = "/sdcard/MUSIC/";
	private RingTonesDB rDB;
	private ListView sqlite_ringtones,store_ringtones;
	private ImageView rt_add,rt_back;
	private TextView rt_title;

	private DataAccessHelper da= new DataAccessHelper(ConfigServer.WEBSERVICE);
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_ringtones, null);
		
		sqlite_ringtones = (ListView)view.findViewById(R.id.sqlite_ringtones);
		store_ringtones = (ListView)view.findViewById(R.id.store_ringtones);
		rDB = new RingTonesDB(getActivity());
		ringtones = rDB.getRingtones();
		adapterSqliteRingtones = new ListSqliteRingtonesAdapter(getActivity(), ringtones);

		sqlite_ringtones.setAdapter(adapterSqliteRingtones);
		rt_add = (ImageView)view.findViewById(R.id.rt_add);
		rt_title = (TextView)view.findViewById(R.id.rt_title);
		rt_back = (ImageView)view.findViewById(R.id.rt_back);
		rt_add.setOnClickListener(this);
		rt_back.setOnClickListener(this);
		SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        sqlite_ringtones,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                	 rDB.delete(ringtones.get(position));
                                    ringtones.remove(ringtones.get(position));
                                   
                                }
                                adapterSqliteRingtones.notifyDataSetChanged();
                            }
                        });
        sqlite_ringtones.setOnTouchListener(touchListener);
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
			rt_title.setText(getResources().getString(R.string.select_ringtone));
			rt_add.setVisibility(View.GONE);
			rt_back.setVisibility(View.VISIBLE);
			ringtones.clear();
			ringtones = getRingtonesByPath();
			adapterStoreRingtones = new ListStoreRingtonesAdapter(getActivity(), ringtones);
			store_ringtones.setAdapter(adapterStoreRingtones);
			store_ringtones.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					arg1.setSelected(true);
					final int pos =  arg2;
					final Dialog dialog = new Dialog(getActivity());
					dialog.setContentView(R.layout.confirm_ringtones);
					dialog.setTitle(getResources().getString(R.string.select_ringtone_type));
					final ImageButton call = (ImageButton)dialog.findViewById(R.id.rt_store_call);
					final ImageButton message = (ImageButton)dialog.findViewById(R.id.rt_store_message);
					final ImageButton notify = (ImageButton)dialog.findViewById(R.id.rt_store_notify);
					call.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ringtones.get(pos).setType(1);
							rDB.insertRingtone(ringtones.get(pos));
							dialog.dismiss();
						}
					});
					message.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ringtones.get(pos).setType(2);
							rDB.insertRingtone(ringtones.get(pos));
							dialog.dismiss();
						}
					});
					notify.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							ringtones.get(pos).setType(3);
							rDB.insertRingtone(ringtones.get(pos));
							dialog.dismiss();
						}
					});
					dialog.show();
					
				}
				
			});

			break;
		case R.id.rt_back:
			if(adapterStoreRingtones!=null)adapterStoreRingtones.stopRingTone();
			sqlite_ringtones.setVisibility(View.VISIBLE);
			store_ringtones.setVisibility(View.GONE);
			rt_title.setText(getResources().getString(R.string.ringtone));
			rt_add.setVisibility(View.VISIBLE);
			rt_back.setVisibility(View.GONE);
			ringtones.clear();
			ringtones = rDB.getRingtones();
			adapterSqliteRingtones = new ListSqliteRingtonesAdapter(getActivity(), ringtones);
			sqlite_ringtones.setAdapter(adapterSqliteRingtones);
			
			break;
		default:
			break;
		}		
	}
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		if(adapterStoreRingtones!=null)adapterStoreRingtones.stopRingTone();
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(adapterStoreRingtones!=null)adapterStoreRingtones.stopRingTone();
	}

}
