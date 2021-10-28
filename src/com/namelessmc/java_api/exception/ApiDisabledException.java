package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.NamelessException;

public class ApiDisabledException extends NamelessException {

	public ApiDisabledException() {
		super("API is disabled, please enable it in StaffCP > Configuration > API");
	}

}
