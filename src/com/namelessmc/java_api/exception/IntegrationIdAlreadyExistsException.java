package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class IntegrationIdAlreadyExistsException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public IntegrationIdAlreadyExistsException() {
		super(ApiError.INTEGRATION_ID_ALREADY_EXISTS);
	}

}
