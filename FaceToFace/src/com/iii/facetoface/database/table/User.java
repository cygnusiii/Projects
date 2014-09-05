package com.iii.facetoface.database.table;

import java.io.Serializable;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name, number, IMEI;

	public User(String name, String number, String IMEI) {
		this.name = name;
		this.number = number;
		this.IMEI = IMEI;
	}
	public User(String value){
		String[] user= value.split("--");
		this.name = user[0];
		this.number = user[1];
		this.IMEI = user[2];
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getIMEI() {
		return IMEI;
	}

	public void setIMEI(String iMEI) {
		IMEI = iMEI;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof User) {
			return ((User) o).name.equals(this.name) && ((User) o).IMEI.equals(this.IMEI) && ((User) o).number.equals(this.number);
		}return false;
	}

	@Override
	public int hashCode() {
		return this.IMEI.hashCode();
	}

}
