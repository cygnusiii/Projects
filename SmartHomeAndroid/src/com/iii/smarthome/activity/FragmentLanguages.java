package com.iii.smarthome.activity;

import java.util.ArrayList;
import java.util.Locale;

import com.iii.smarthome.R;
import com.iii.smarthome.activity.adapter.ListLanguageAdapter;
import com.iii.smarthome.database.LanguagesDB;
import com.iii.smarthome.database.table.Language;
import com.iii.smarthome.database.table.Settings;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FragmentLanguages extends Fragment {
	
	private LanguagesDB dB;
	private ArrayList<Language> languages;
	private ListLanguageAdapter adapter_language;
	private ListView list_language;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_language, container,false);
		list_language = (ListView)view.findViewById(R.id.list_language);
		list_language.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		if(languages==null){
			languages = new ArrayList<Language>();	
			adapter_language = new ListLanguageAdapter(getActivity(), languages);
			dB = new LanguagesDB(getActivity());

			for(int i=0;i<getResources().getStringArray(R.array.language_code).length;i++){
				if(dB.getLanguageCode().equals(getResources().getStringArray(R.array.language_code)[i])){
					languages.add(new Language(getResources().getStringArray(R.array.language_name)[i],getResources().getStringArray(R.array.language_code)[i],getResources().obtainTypedArray(R.array.language_image).getResourceId(i, -1),true));
				}else
				languages.add(new Language(getResources().getStringArray(R.array.language_name)[i],getResources().getStringArray(R.array.language_code)[i],getResources().obtainTypedArray(R.array.language_image).getResourceId(i, -1)));
			}
		}

		list_language.setAdapter(adapter_language);
		return view;
	}


}
