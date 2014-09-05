package com.iii.facetoface.activity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iii.facetoface.R;

public class CustomViewListUser extends LinearLayout {
    public TextView user_name;
    public TextView user_number;

    public CustomViewListUser(Context context) {
    	super(context);
        LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
        li.inflate(R.layout.user_row, this, true);
        user_name = (TextView)findViewById(R.id.user_name);
        user_number= (TextView)findViewById(R.id.user_number);

    }
}

