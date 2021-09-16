package com.namelessmc.java_api;

/**
 * Generic exception thrown by many methods in the Nameless API
 */
public class NamelessException extends Exception {

	private static final long serialVersionUID = -3698433855091611529L;

	public NamelessException(final String message) {
		super(message);
	}

	public NamelessException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public NamelessException(final Throwable cause) {
		super(cause);
	}

	public NamelessException() {
		super();
	}

}
