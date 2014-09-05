package com.iii.smarthome.activity.adapter;

import java.util.ArrayList;
import java.util.Locale;

import com.iii.smarthome.R;
import com.iii.smarthome.activity.ClientActivity;
import com.iii.smarthome.database.LanguagesDB;
import com.iii.smarthome.database.table.Language;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

public class ListLanguageAdapter extends BaseAdapter{
	private Context mContext;
	private LanguagesDB dB ;
	private ArrayList<Language> mLanguages;
	public ListLanguageAdapter(Context context, ArrayList<Language> languages) {
		super();
		this.mContext = context;
		this.mLanguages = languages;
		this.dB = new LanguagesDB(context);
	}
	@Override
	public int getCount() {
		return mLanguages.size();
	}
	@Override
	public Object getItem(int position) {		
		return mLanguages.get(position);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Language language = (Language) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.language_row, parent, false);
			holder.image = (ImageView)convertView.findViewById(R.id.language_image);
			holder.name = (TextView) convertView.findViewById(R.id.language_name);
			holder.check = (RadioButton)convertView.findViewById(R.id.language_check);
			
			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();
		holder.image.setImageResource(language.getResource());
		holder.name.setText(language.getName());
		holder.check.setChecked(language.isChecked());
		final ListLanguageAdapter self = this;
		holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				setLocale(language);
			}
		});
		return convertView;
	}
	private static class ViewHolder
	{
		TextView name;
		ImageView image;
		RadioButton check;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private void setLocale(Language language) {
		Locale myLocale = new Locale(language.getCode());
		Resources res = mContext.getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		Configuration conf = res.getConfiguration();
		conf.locale = myLocale;
		res.updateConfiguration(conf, dm);
		dB.update(language);
		Intent refresh = new Intent(mContext, ClientActivity.class);
		mContext.startActivity(refresh);
		((ClientActivity) mContext).finish();

	}

}
