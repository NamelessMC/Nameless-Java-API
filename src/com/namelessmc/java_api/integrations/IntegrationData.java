package com.namelessmc.java_api.integrations;

import org.jetbrains.annotations.NotNull;

public class IntegrationData {

	private final @NotNull String integrationType;
	private final @NotNull String identifier;
	private final @NotNull String username;

	public IntegrationData(final @NotNull String integrationType,
					final @NotNull String identifier,
					final @NotNull String username) {
		this.integrationType = integrationType;
		this.identifier = identifier;
		this.username = username;
	}

	public @NotNull String getIntegrationType() {
		return this.integrationType;
	}

	public @NotNull String getIdentifier() {
		return this.identifier;
	}

	public @NotNull String getUsername() {
		return this.username;
	}

}
