package com.iii.smarthome.database.table;

import java.io.Serializable;


public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name,time,message,voice,title;
	private int id;
	public Message(){}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}
	public boolean hasMessage(){
		return !message.isEmpty();
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getVoice() {
		return voice;
	}
	public void setVoice(String voice) {
		this.voice = voice;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
