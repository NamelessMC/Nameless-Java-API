package com.namelessmc.java_api.integrations;

import org.jetbrains.annotations.NotNull;

public abstract class IntegrationData {

	private final @NotNull IntegrationType integrationType;

	IntegrationData(final @NotNull IntegrationType integrationType) {
		this.integrationType = integrationType;
	}

	public @NotNull IntegrationType getIntegrationType() {
		return this.integrationType;
	}

	public abstract @NotNull String getRawId();

	public abstract @NotNull String getRawUsername();

}
