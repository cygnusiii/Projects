package com.iii.facetoface.activity.adapter;

import com.iii.facetoface.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomViewStoreRingtones extends LinearLayout {
    public CheckBox cb;
    public TextView name;
    public CustomViewStoreRingtones(Context context) {
    	super(context);
        LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        li.inflate(R.layout.store_ringtones, this, true);
        cb = (CheckBox) findViewById(R.id.rt_store_check);
        name = (TextView)findViewById(R.id.rt_store_name);
    }
}
