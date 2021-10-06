package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

/**
 * Generic exception thrown by many methods in the Nameless API
 */
public class NamelessException extends Exception {

	private static final long serialVersionUID = -3698433855091611529L;

	public NamelessException(@NotNull final String message) {
		super(message);
	}

	public NamelessException(@NotNull final String message, @NotNull final Throwable cause) {
		super(message, cause);
	}

	public NamelessException(@NotNull final Throwable cause) {
		super(cause);
	}

	public NamelessException() {
		super();
	}

}
