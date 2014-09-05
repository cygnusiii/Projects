package com.iii.smarthome.database.table;

public class Language {
	private String name;
	private String code;
	private boolean isChecked;
	private int resource;
	public Language(String name, String code, int resource) {
		super();
		this.name = name;
		this.code = code;
		this.resource = resource;
	}
	public Language(String name, String code, int resource,boolean isChecked) {
		super();
		this.name = name;
		this.code = code;
		this.resource = resource;
		this.isChecked = isChecked;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public int getResource() {
		return resource;
	}
	public void setResource(int resource) {
		this.resource = resource;
	}
	
}
