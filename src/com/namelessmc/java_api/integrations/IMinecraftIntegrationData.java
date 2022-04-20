package com.namelessmc.java_api.integrations;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public interface IMinecraftIntegrationData {

	@NonNull UUID getUniqueId();

}
