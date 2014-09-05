package com.iii.smarthome.database.table;

public class Notify {
	private String time,message,messageUrl;

	public Notify(String time, String message, String messageUrl) {
		super();
		this.time = time;
		this.message = message;
		this.messageUrl = messageUrl;
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

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageUrl() {
		return messageUrl;
	}

	public void setMessageUrl(String messageUrl) {
		this.messageUrl = messageUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result
				+ ((messageUrl == null) ? 0 : messageUrl.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notify other = (Notify) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (messageUrl == null) {
			if (other.messageUrl != null)
				return false;
		} else if (!messageUrl.equals(other.messageUrl))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}
	
}
