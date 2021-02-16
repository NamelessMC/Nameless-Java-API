package com.namelessmc.java_api;

public class Notification {

	private final String message;
	private final String url;
	private final NotificationType type;

	public Notification(final String message, final String url, final NotificationType type) {
		this.message = message;
		this.url = url;
		this.type = type;
	}

	public String getMessage() {
		return this.message;
	}

	public String getUrl() {
		return this.url;
	}

	public NotificationType getType() {
		return this.type;
	}

	public enum NotificationType {

		TAG,
		MESSAGE,
		LIKE,
		PROFILE_COMMENT,
		COMMENT_REPLY,
		THREAD_REPLY,
		FOLLOW,

		UNKNOWN;

		public static NotificationType fromString(final String string) {
			try {
				return NotificationType.valueOf(string.replace('-', '_').toUpperCase());
			} catch (final IllegalArgumentException e) {
				return NotificationType.UNKNOWN;
			}
		}

	}

}
