package com.namelessmc.java_api.integrations;

import org.checkerframework.checker.initialization.qual.UnknownInitialization;
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

	public final @NonNull String getIntegrationType(@UnknownInitialization(IntegrationData.class) IntegrationData this) {
		return this.integrationType;
	}

	public final @NonNull String getIdentifier(@UnknownInitialization(IntegrationData.class) IntegrationData this) {
		return this.identifier;
	}

	public final @NonNull String getUsername(@UnknownInitialization(IntegrationData.class) IntegrationData this) {
		return this.username;
	}

}
