package com.namelessmc.java_api.exception;

public class UnknownNamelessVersionException extends NamelessException {

	public UnknownNamelessVersionException(String versionName, String reason) {
		super("Unknown Nameless version:" + versionName + ": " + reason);
	}

}
