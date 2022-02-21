package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

public enum IntegrationType {

	MINECRAFT("Minecraft"),
	DISCORD("Discord"),
	;

	private final @NotNull String apiValue;

	IntegrationType(final @NotNull String apiValue) {
		this.apiValue = apiValue;
	}

	public @NotNull String apiValue() {
		return apiValue;
	}

}
