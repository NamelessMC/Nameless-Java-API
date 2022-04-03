package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class IntegrationUsernameAlreadyExistsException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public IntegrationUsernameAlreadyExistsException() {
		super(ApiError.INTEGRATION_USERNAME_ALREADY_EXISTS);
	}

}
