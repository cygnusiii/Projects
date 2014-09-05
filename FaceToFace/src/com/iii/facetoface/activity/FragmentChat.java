package com.iii.facetoface.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.iii.facetoface.R;
import com.iii.facetoface.activity.adapter.ListMessageAdapter;
import com.iii.facetoface.activity.adapter.ListUserAdapter;
import com.iii.facetoface.conference.AppChat;
import com.iii.facetoface.database.table.User;
import com.iii.facetoface.database.table.Message;
import com.iii.facetoface.webservice.ConfigChannel;
import com.iii.facetoface.webservice.Encrypt;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentChat extends Fragment {
	ArrayList<Message> messages;
	HashMap<User, ArrayList<Message>> hashMessage;
	ArrayList<User> users;
	ListView list_msg,list_user;
	ListMessageAdapter adapter_msg;
	ListUserAdapter adapter_user;
	EditText text_msg;
	Button send_msg;ImageButton bt_call;
	AppChat appChat;
	int pos_user = -1;
	private TextView user_name_selected, user_number_selected;
	private String offerIMEI,myIMEI,offerName,myName,myNumber,offerNumber;
	private BroadcastReceiver receiver;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		try{
			appChat = AppChat.getInstance();
			container.removeAllViews();
		}catch(Exception ex){}
		View view  = inflater.inflate(R.layout.fragment_chat,container,false);
		text_msg = (EditText)view.findViewById(R.id.text_msg);
		list_msg = (ListView)view.findViewById(R.id.list_msg);
		send_msg = (Button)view.findViewById(R.id.send_msg);bt_call = (ImageButton)view.findViewById(R.id.bt_call);
		user_name_selected = (TextView)getActivity().findViewById(R.id.user_name_selected);
		user_number_selected = (TextView)getActivity().findViewById(R.id.user_number_selected);
		if(messages==null){
			hashMessage = new HashMap<User, ArrayList<Message>>();
			messages = new ArrayList<Message>();
			adapter_msg = new ListMessageAdapter(getActivity(), messages);			
		}
		messages.clear();
		adapter_msg.notifyDataSetChanged();
		list_msg.setAdapter(adapter_msg);
		send_msg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendMessage();
			}
		});
		bt_call.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendCall();
			}
		});
		//list user chat
		list_user = (ListView)view.findViewById(R.id.list_user);
		users = new ArrayList<User>();
		adapter_user = new ListUserAdapter(getActivity(), users);			
		list_user.setAdapter(adapter_user);
		list_user.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				arg1.setSelected(true);pos_user=arg2;
				messages.clear();
				adapter_msg.notifyDataSetChanged();
				if(hashMessage!=null&&hashMessage.get(users.get(pos_user))!=null){
					messages.addAll(hashMessage.get(users.get(pos_user)));
					adapter_msg.notifyDataSetChanged();
					list_msg.setSelection(messages.size()-1);
					
				}
				user_name_selected.setText(users.get(arg2).getName().toString());
				user_number_selected.setText(users.get(arg2).getNumber().toString());
						
			}
			
		});
		//receive users
		requestService("users");
		//receive messages
		requestService("messages");
		// register & define filter for local listener
		IntentFilter i = new IntentFilter("com.iii.facetoface.action.responseClient"); 
		receiver = new reponseReceiver(); 
		getActivity().registerReceiver(receiver, i);
		
		return view;
	}
	public void setMyProfile(String myName,String myIMEI,String myNumber){
		this.myName = myName;this.myIMEI = myIMEI;this.myNumber = myNumber;
	}
	public void sendMessage()
	{
		
		String newMessage = text_msg.getText().toString().trim(); 
		if(newMessage.length() > 0&&pos_user!=-1)
		{
			text_msg.setText("");
			addNewMessage(new Message(newMessage, true));
			appChat.sendMessage(Encrypt.getMD5(ConfigChannel.PREFIX_CHAT + users.get(pos_user).getIMEI()) + newMessage);
			
		}else Toast.makeText(getActivity(), "Bạn chưa chọn ai trong danh sách ",Toast.LENGTH_SHORT).show();
	}

	private void sendCall(){
		if(pos_user!=-1){
			appChat.sendMessage(Encrypt.getMD5(ConfigChannel.OFFER_VIDEO + users.get(pos_user).getIMEI()));
			offerIMEI = users.get(pos_user).getIMEI(); offerName = users.get(pos_user).getName();offerNumber =  users.get(pos_user).getNumber();
			//start video activity
			Intent i = new Intent(getActivity(),VideoActivity.class);
			i.putExtra("myName", myName);
			i.putExtra("myIMEI", myIMEI);
			i.putExtra("myNumber", myNumber);
			i.putExtra("offerName", offerName);
			i.putExtra("offerIMEI", offerIMEI);
			i.putExtra("offerNumber", offerNumber);
			i.putExtra("isComming", false);
			startActivity(i);
		}else Toast.makeText(getActivity(), "Bạn chưa chọn ai trong danh sách ",Toast.LENGTH_SHORT).show();
	}
	void addNewMessage(Message m)
	{
		messages.add(m);
		adapter_msg.notifyDataSetChanged();
		list_msg.setSelection(messages.size()-1);
		if(hashMessage!=null)hashMessage.put(users.get(pos_user), new ArrayList<Message>(messages));
	}
	///user chat
	void addNewUser(User u)
	{
		users.add(u);
		adapter_user.notifyDataSetChanged();
		
	}
	void removeUser(User u){
		users.remove(u);
		adapter_user.notifyDataSetChanged();
	}
	private void requestService(String request){
		Intent intent = new Intent("com.iii.facetoface.action.requestClient");
		intent.putExtra("request", request);
		getActivity().sendBroadcast(intent);
	}
	public class reponseReceiver extends BroadcastReceiver {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			
			final String serviceData = intent.getStringExtra("serviceData");
			//add user
			if(serviceData.contains("attach-room")){
				User u = new User(serviceData.replace("attach-room", ""));				
				if(!users.contains(u)){
					addNewUser(u);if(pos_user==-1)list_user.setSelection(0);
				}
			//remove user
			}else if (serviceData.contains("detach-room")){
				User u = new User(serviceData.replace("detach-room", ""));				
				removeUser(u);
				if(users.size()>0)send_msg.setEnabled(true);
				else send_msg.setEnabled(false);
			}else if(serviceData.contains("receive-messages")){
				hashMessage = (HashMap<User, ArrayList<Message>>) intent.getSerializableExtra("hashMessage");
			}else if (serviceData.contains(Encrypt.getMD5(ConfigChannel.PREFIX_CHAT + myIMEI))){
				User u = (User)intent.getSerializableExtra("offerUser");
				String message = serviceData.replace(Encrypt.getMD5(ConfigChannel.PREFIX_CHAT + myIMEI), "");
				if(hashMessage.get(u)!=null){
					hashMessage.get(u).add(new Message(message, false));
					
				}
				else {
					//put msg for new user
					ArrayList<Message> temp  = new ArrayList<Message>();
					temp.add(new Message(message, false));
					hashMessage.put(u, temp);
				}
				//display unread messages
				if(getUnReadMessages(hashMessage.get(u))>0){
					Toast.makeText(getActivity(), u.getName() +" "+ getUnReadMessages(hashMessage.get(u)) + " new messages", Toast.LENGTH_SHORT).show();
				}
				//update msg for user selected
				messages.clear();
				adapter_msg.notifyDataSetChanged();
				if(hashMessage.get(users.get(pos_user))!=null){
					//mark read messages 
					markReadMessages(hashMessage.get(users.get(pos_user)));
					messages.addAll(hashMessage.get(users.get(pos_user)));
					adapter_msg.notifyDataSetChanged();
					list_msg.setSelection(messages.size()-1);
					
				}
			}
		}
	}
	private void markReadMessages(ArrayList<Message> messages){
		for(Message msg:messages){
			msg.setStatusMessage(true);
		}
	}
	private int getUnReadMessages(ArrayList<Message> messages){
		int count = 0;
		for(Message msg:messages){
			if(!msg.isStatusMessage())count++;
		}
		return count;
	}
}
