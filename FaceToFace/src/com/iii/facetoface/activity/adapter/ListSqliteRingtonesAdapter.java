package com.iii.facetoface.activity.adapter;

import java.util.ArrayList;

import com.iii.facetoface.database.RingTonesDB;
import com.iii.facetoface.database.table.Ringtone;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ListSqliteRingtonesAdapter extends ArrayAdapter<Ringtone> {
    private ArrayList<Ringtone> array;
    private int resource;
    private Context context;
    private Dialog dialog ;
	public ListSqliteRingtonesAdapter(Context context, int textViewResourceId,ArrayList<Ringtone> objects) {
		// TODO Auto-generated constructor stub
		 super(context, textViewResourceId, objects);
	     this.context = context;
	     resource = textViewResourceId;
	     array = objects;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
        View userView = convertView;
        if (userView == null) {
        	userView = new CustomViewSqliteRingtones(getContext());
        }
        final Ringtone ringtone = array.get(position);
        if (ringtone != null) {
        	TextView id = ((CustomViewSqliteRingtones)userView).id;
        	TextView time_play= ((CustomViewSqliteRingtones)userView).time_play;
            TextView name = ((CustomViewSqliteRingtones)userView).name;
            TextView path = ((CustomViewSqliteRingtones)userView).path;
            final CheckBox checkRing = ((CustomViewSqliteRingtones)userView).cb;
            checkRing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            	@Override
            	public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            		ringtone.setStatus(isChecked);
                    ///-------------- cap nhat DB--------------//
            		new RingTonesDB(getContext()).selectRingtone(ringtone);
            	} 
            });
            id.setText(Integer.toString(ringtone.getId()));
            time_play.setText(Integer.toString(ringtone.getTime_play()));
            name.setText(ringtone.getName());
            path.setText(ringtone.getPath());
            checkRing.setChecked(ringtone.getStatus());
        }
		return userView;
	}

}
