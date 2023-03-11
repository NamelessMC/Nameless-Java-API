package com.namelessmc.java_api.exception;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ApiException extends NamelessException {

	private static final long serialVersionUID = 1L;

	private final ApiError apiError;

	public ApiException(final ApiError apiError, final @Nullable String meta) {
		super("API error " + apiError + (meta == null ? "" : " (meta: " + meta + ")"));
		this.apiError = apiError;
	}

	public ApiError apiError() {
		return this.apiError;
	}

}
