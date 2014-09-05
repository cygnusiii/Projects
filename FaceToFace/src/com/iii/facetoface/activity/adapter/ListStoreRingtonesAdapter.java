package com.iii.facetoface.activity.adapter;

import java.util.ArrayList;

import com.iii.facetoface.database.RingTonesDB;
import com.iii.facetoface.database.table.Ringtone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ListStoreRingtonesAdapter extends ArrayAdapter<Ringtone> {
	private ArrayList<Ringtone> array;
    private int resource;
    private Context context;
    private Dialog dialog ;
	public ListStoreRingtonesAdapter(Context context, int textViewResourceId,ArrayList<Ringtone> objects) {
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
        	userView = new CustomViewStoreRingtones(getContext());
        }
        final int pos = position;
        final Ringtone ringtone = array.get(position);
        if (ringtone != null) {
            final TextView name = ((CustomViewStoreRingtones)userView).name;
            final CheckBox checkRing = ((CustomViewStoreRingtones)userView).cb;
            checkRing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            	@Override
            	public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            		ringtone.setStatus(isChecked);
                   
            		
            	} 
            });
            name.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
					builder.setTitle("Thông báo");
					builder.setMessage("Chọn bài này làm nhạc chuông ?");
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							 ///-------------- cap nhat DB--------------//
							RingTonesDB rDb = new RingTonesDB(getContext());
							if(rDb.getNumRow()==1)rDb.update(ringtone);
							else {
								rDb.delete();
								rDb.insert(ringtone);
							}
						}
					});
					builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							
						}
					});
					builder.show();
				}
			});
            name.setText(ringtone.getName());
            checkRing.setChecked(ringtone.getStatus());
        }
		return userView;
	}
}
