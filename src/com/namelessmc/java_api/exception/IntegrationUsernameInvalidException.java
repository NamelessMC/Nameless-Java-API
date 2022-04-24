package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class IntegrationUsernameInvalidException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public IntegrationUsernameInvalidException() {
		super(ApiError.INTEGRATION_USERNAME_INVALID);
	}

}
