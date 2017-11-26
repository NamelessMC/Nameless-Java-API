package com.namelessmc.NamelessAPI;

public class Notification {
	
	private String message;
	private String url;
	private NotificationType type;
	
	public Notification(String message, String url, NotificationType type) {
		this.message = message;
		this.url = url;
		this.type = type;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getUrl() {
		return url;
	}
	
	public NotificationType getType() {
		return type;
	}
	
	public static enum NotificationType {
		
		MESSAGE,
		LIKE,
		PROFILE_COMMENT,
		COMMENT_REPLY,
		THREAD_REPLY,
		FOLLOW;
		
		public static NotificationType fromString(String string) {
			return NotificationType.valueOf(string.replace('-', '_').toUpperCase());
		}
		
	}

}
