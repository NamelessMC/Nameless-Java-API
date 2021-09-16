package main.java.com.namelessmc.java_api.exception;

import main.java.com.namelessmc.java_api.ApiError;

public class CannotReportSelfException extends ApiErrorException {

	private static final long serialVersionUID = 1L;

	public CannotReportSelfException() {
		super(ApiError.CANNOT_REPORT_YOURSELF);
	}

}
