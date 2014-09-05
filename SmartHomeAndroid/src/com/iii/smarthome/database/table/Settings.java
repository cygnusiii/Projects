package com.iii.smarthome.database.table;

import android.content.res.TypedArray;

public class Settings {
	private String name;
	private int resource;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getResource() {
		return resource;
	}
	public void setResource(int resource) {
		this.resource = resource;
	}
	public Settings(String name, int resource) {
		super();
		this.name = name;
		this.resource = resource;
	}
}
