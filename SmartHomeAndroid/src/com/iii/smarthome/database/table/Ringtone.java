package com.iii.smarthome.database.table;

public class Ringtone {
	private String name;
	private String path;
	private boolean play;
	private int time;
	private int type;
	public Ringtone(String name, String path,int time,int type) {
		super();
		this.name = name;
		this.path = path;
		this.time = time;
		this.type = type;
	}
	public Ringtone(String name, String path, int time) {
		super();
		this.name = name;
		this.path = path;

		this.time = time;
	}
	public Ringtone(String name, String path) {
		super();
		this.time = 30;
		this.name = name;
		this.path = path;
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
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public boolean isPlay() {
		return play;
	}
	public void setPlay(boolean play) {
		this.play = play;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
