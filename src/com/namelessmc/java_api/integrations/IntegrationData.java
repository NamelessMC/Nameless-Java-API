package com.namelessmc.java_api.integrations;

import org.checkerframework.checker.nullness.qual.NonNull;

public class IntegrationData {

	private final @NonNull String integrationType;
	private final @NonNull String identifier;
	private final @NonNull String username;

	public IntegrationData(final @NonNull String integrationType,
					final @NonNull String identifier,
					final @NonNull String username) {
		this.integrationType = integrationType;
		this.identifier = identifier;
		this.username = username;
	}

	public @NonNull String getIntegrationType() {
		return this.integrationType;
	}

	public @NonNull String getIdentifier() {
		return this.identifier;
	}

	public @NonNull String getUsername() {
		return this.username;
	}

}
