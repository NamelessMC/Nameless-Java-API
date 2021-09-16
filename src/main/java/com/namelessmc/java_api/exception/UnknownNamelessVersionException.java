package com.namelessmc.java_api.exception;

public class UnknownNamelessVersionException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnknownNamelessVersionException(final String versionString) {
		super("Cannot parse version string '" + versionString + "'. Try updating the API or the software using it.");
	}

}
