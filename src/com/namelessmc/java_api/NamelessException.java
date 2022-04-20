package com.namelessmc.java_api;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Generic exception thrown by many methods in the Nameless API
 */
public class NamelessException extends Exception {

	private static final long serialVersionUID = -3698433855091611529L;

	public NamelessException(final @NonNull String message) {
		super(message);
	}

	public NamelessException(final @NonNull String message, final @NonNull Throwable cause) {
		super(message, cause);
	}

	public NamelessException(final @NonNull Throwable cause) {
		super(cause);
	}

	public NamelessException() {
		super();
	}

}
