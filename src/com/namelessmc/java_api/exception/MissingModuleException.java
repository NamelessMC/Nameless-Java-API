package com.namelessmc.java_api.exception;

public class MissingModuleException extends NamelessException {

	private static final long serialVersionUID = 1L;

	public MissingModuleException(final String moduleName) {
		super("Required module not installed: " + moduleName);
	}

}
