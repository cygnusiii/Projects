package com.iii.facetoface.activity.adapter;
import com.iii.facetoface.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomViewSqliteRingtones extends LinearLayout {
    public CheckBox cb;
    public TextView id;
    public TextView name;
    public TextView path;
    public TextView time_play;
    public CustomViewSqliteRingtones(Context context) {
    	super(context);
        LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        li.inflate(R.layout.sqlite_ringtones, this, true);
        id = (TextView)findViewById(R.id.rt_id);
        cb = (CheckBox) findViewById(R.id.rt_check);
        name = (TextView)findViewById(R.id.rt_name);
        path = (TextView)findViewById(R.id.rt_path);
        time_play =(TextView)findViewById(R.id.rt_time_play);
    }
}
