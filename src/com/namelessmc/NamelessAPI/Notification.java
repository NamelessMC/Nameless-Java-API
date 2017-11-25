package com.namelessmc.NamelessAPI;

public class Notification {
	
	private String message;
	private String url;
	
	public Notification(String message, String url) {
		this.message = message;
		this.url = url;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getUrl() {
		return url;
	}

}
