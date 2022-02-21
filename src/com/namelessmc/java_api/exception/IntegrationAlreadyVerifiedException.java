package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class IntegrationAlreadyVerifiedException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public IntegrationAlreadyVerifiedException() {
		super(ApiError.INTEGRATION_ALREADY_VERIFIED);
	}

}
