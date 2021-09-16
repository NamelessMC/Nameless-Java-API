package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class InvalidUsernameException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public InvalidUsernameException() {
		super(ApiError.INVALID_USERNAME);
	}

}
