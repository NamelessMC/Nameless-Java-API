package com.namelessmc.java_api.exception;

import com.namelessmc.java_api.modules.NamelessModule;

public class MissingModuleException extends NamelessException {

	private static final long serialVersionUID = 1L;

	public MissingModuleException(final NamelessModule module) {
		super(getExceptionMessage(module));
	}

	private static String getExceptionMessage(final NamelessModule module) {
		StringBuilder builder = new StringBuilder("Required module not installed: ");
		builder.append(module.name());
		builder.append(".");

		if (module.isIncluded()) {
			builder.append(" This module is an official module, included with NamelessMC. Please enable it.");
		} else {
			builder.append(" This module is a third-party module.");
			if (module.downloadLink() != null) {
				builder.append(" It can be downloaded here: ");
				builder.append(module.downloadLink());
			} else {
				builder.append(" It can be downloaded externally.");
			}
		}

		return builder.toString();
	}

}
