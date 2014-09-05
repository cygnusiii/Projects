package com.iii.facetoface.database.table;

public class Language {
	private int id;
	private String name;
	private String code;

	public Language(String name, String code) {
		super();
		this.name = name;
		this.code = code;
	}
	public int getId() {
		return id;
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
}
