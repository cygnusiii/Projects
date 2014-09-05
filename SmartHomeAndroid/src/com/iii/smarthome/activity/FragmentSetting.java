package com.iii.smarthome.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.iii.smarthome.R;
import com.iii.smarthome.activity.adapter.ListSettingAdapter;
import com.iii.smarthome.database.table.Settings;

import android.R.anim;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

@SuppressLint("NewApi")
public class FragmentSetting extends Fragment {
	private ListView list_setting;
	private ArrayList<Settings> settings;
	private ListSettingAdapter adapter_setting;
	private FragmentLanguages fragLanguage;
	private FragmentRingtones fragRingtone;
	private FragmentTransaction fragmentTran = null;
	private LinearLayout ln;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view  = inflater.inflate(R.layout.fragment_setting, container,false);
		list_setting = (ListView)view.findViewById(R.id.list_setting);
		
		ln = (LinearLayout)view.findViewById(R.id.container_setting);
		if(settings==null){
			settings = new ArrayList<Settings>();
			adapter_setting = new ListSettingAdapter(getActivity(), settings);
			for(int i=0;i<getResources().getStringArray(R.array.settings_name).length;i++){
				settings.add(new Settings(getResources().getStringArray(R.array.settings_name)[i],getResources().obtainTypedArray(R.array.settings_image).getResourceId(i, -1)));
			}
		}
		list_setting.setAdapter(adapter_setting);
		//language is default
		if(fragLanguage == null)fragLanguage = new FragmentLanguages();
		fragmentTran = getChildFragmentManager().beginTransaction();
		fragmentTran.replace(ln.getId(),fragLanguage);
		fragmentTran.commit();
		
		list_setting.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				arg1.setSelected(true);
			
				switch (arg2) {
				case 0:
					//language
					if(fragLanguage == null)fragLanguage = new FragmentLanguages();
					fragmentTran = getChildFragmentManager().beginTransaction();
					fragmentTran.replace(R.id.container_setting,fragLanguage);
					fragmentTran.commit();
					break;
				case 1:
					//ringtones
					if(fragRingtone == null)fragRingtone = new FragmentRingtones();
					fragmentTran = getChildFragmentManager().beginTransaction();
					fragmentTran.replace(ln.getId(),fragRingtone);
					fragmentTran.commit();
					break;
				default:
					break;
				}
			}
		});
		
		new Handler().post(new Runnable() {
		    @Override
		    public void run() {
		    	list_setting.performItemClick(list_setting.getChildAt(0),0,list_setting.getAdapter().getItemId(0));
		    }
		});
		return view;
	}
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		 try {
		        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
		        childFragmentManager.setAccessible(true);
		        childFragmentManager.set(this, null);

		    } catch (NoSuchFieldException e) {
		        throw new RuntimeException(e);
		    } catch (IllegalAccessException e) {
		        throw new RuntimeException(e);
		    }
	}

}
