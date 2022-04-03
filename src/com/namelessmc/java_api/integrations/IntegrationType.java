package com.namelessmc.java_api.integrations;

import java.util.Locale;

public enum IntegrationType {

	MINECRAFT,
	DISCORD,
	;


	@Override
	public String toString() {
		return this.name().toLowerCase(Locale.ROOT);
	}

}
