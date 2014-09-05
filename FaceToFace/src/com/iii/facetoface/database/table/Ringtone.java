package com.iii.facetoface.database.table;

public class Ringtone {
	private int id;
	private String name;
	private String path;
	private Boolean status;
	private int time_play;
	public Ringtone(int id, String name, String path, int status,int time_play) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
		if(status==1){
			this.status=true;
		}else{
			this.status=false;
		}
		this.time_play = time_play;
	}
	public Ringtone(String name, String path, int status, int time_play) {
		super();
		this.name = name;
		this.path = path;

		this.time_play = time_play;
	}
	public Ringtone(String name, String path) {
		super();
		this.status = false;
		this.time_play = 30;
		this.name = name;
		this.path = path;
	}
	public int getId(){
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Boolean getStatus() {
		return status;
	}
	public void setStatus(Boolean status) {
		this.status = status;
	}
	public int getTime_play() {
		return time_play;
	}
	public void setTime_play(int time_play) {
		this.time_play = time_play;
	}
}
