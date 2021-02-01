package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.ApiError;

public class UnableToCreateReportException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public UnableToCreateReportException() {
		super(ApiError.UNABLE_TO_CREATE_REPORT);
	}

}
