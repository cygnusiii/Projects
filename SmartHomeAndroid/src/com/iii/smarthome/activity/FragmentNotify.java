package com.iii.smarthome.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.iii.smarthome.R;
import com.iii.smarthome.activity.adapter.ListMessageAdapter;
import com.iii.smarthome.database.table.Message;
import com.iii.smarthome.webservice.ConfigChannel;
import com.iii.smarthome.webservice.ConfigServer;
import com.iii.smarthome.webservice.DataAccessHelper;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class FragmentNotify extends Fragment {
	private ArrayList<Message> notifies;
	private ListMessageAdapter adapter_notify;
	private ListView list_notify;
	private String myIMEI;
	private DataAccessHelper da = new DataAccessHelper(ConfigServer.WEBSERVICE);
	private BroadcastReceiver receiver;
	private FragmentTransaction fragmentTran = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_notify, container,false);
		list_notify = (ListView)view.findViewById(R.id.list_notify);
		TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		myIMEI = telephonyManager.getDeviceId();
		if(notifies==null){
			notifies = new ArrayList<Message>();
			adapter_notify = new ListMessageAdapter(getActivity(), notifies);
			notifies.addAll(getNotifies());
			adapter_notify.notifyDataSetChanged();
		}
		list_notify.setAdapter(adapter_notify);
		list_notify.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				arg1.setSelected(true);
				FragmentMessageInfo fragMsgInfo = new FragmentMessageInfo();
				Bundle bundle = new Bundle();
				bundle.putSerializable("message", notifies.get(arg2));
				fragMsgInfo.setArguments(bundle);
				fragmentTran = getChildFragmentManager().beginTransaction();
				fragmentTran.replace(R.id.container_notify,fragMsgInfo);
				fragmentTran.commit();
			}
			
		});
		// register & define filter for local listener
		IntentFilter i = new IntentFilter("com.iii.facetoface.action.responseClient"); 
		receiver = new reponseReceiver(); 
		getActivity().registerReceiver(receiver, i);
		
		return view;
	}
	private class reponseReceiver extends BroadcastReceiver {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			
			final String serviceData = intent.getStringExtra("serviceData");
			if(serviceData.contains("notifies")){
				notifies.addAll(getNotifies());
				adapter_notify.notifyDataSetChanged();
			}
			
			
		}
	}
	private ArrayList<Message> getNotifies(){
		ArrayList<Message> result = new ArrayList<Message>();
		String response = da.responseString("get_notifies", myIMEI);
		
		try {
			JSONObject json = new JSONObject(response);
			JSONArray jNotifies  = json.getJSONArray("Notifies");
			for(int i= 0 ;i<jNotifies.length();i++){
				JSONObject o = jNotifies.getJSONObject(i);
				Message m = new Message();
				m.setId(o.getInt("id"));
				m.setTitle(o.getString("title"));
				m.setName(getResources().getString(R.string.administrator));
				m.setMessage(o.getString("message"));
				m.setTime(o.getString("time"));
				m.setVoice(o.getString("voice"));
				result.add(m);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
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
