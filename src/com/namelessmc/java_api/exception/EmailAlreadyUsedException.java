package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class EmailAlreadyUsedException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public EmailAlreadyUsedException() {
		super(ApiError.EMAIL_ALREADY_EXISTS);
	}

}
