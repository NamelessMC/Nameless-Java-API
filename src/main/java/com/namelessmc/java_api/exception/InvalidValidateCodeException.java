package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class InvalidValidateCodeException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public InvalidValidateCodeException() {
		super(ApiError.INVALID_VALIDATE_CODE);
	}

}
