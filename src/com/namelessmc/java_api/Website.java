package com.namelessmc.java_api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.stream.StreamSupport;

public class Website implements LanguageEntity {

	private final @NonNull String version;
	private final @Nullable Update update;
	private final @NonNull String@NonNull[] modules;
	private final @NonNull String rawLanguage;

	Website(final @NonNull JsonObject json) {
		Objects.requireNonNull(json, "Provided json object is null");

		this.version = json.get("nameless_version").getAsString();

		this.modules = StreamSupport.stream(json.get("modules").getAsJsonArray().spliterator(), false)
				.map(JsonElement::getAsString)
				.toArray(String[]::new);

		if (json.has("version_update")) {
			final JsonObject updateJson = json.get("version_update").getAsJsonObject();
			final boolean updateAvailable = updateJson.get("update").getAsBoolean();
			if (updateAvailable) {
				final String updateVersion = updateJson.get("version").getAsString();
				final boolean isUrgent = updateJson.get("urgent").getAsBoolean();
				this.update = new Update(isUrgent, updateVersion);
			} else {
				this.update = null;
			}
		} else {
			this.update = null;
		}

		this.rawLanguage = json.get("locale").getAsString();
	}

	public @NonNull String rawVersion() {
		return this.version;
	}

	public @Nullable NamelessVersion parsedVersion() {
		return NamelessVersion.parse(this.version);
	}

	/**
	 * @return Information about an update, or empty if no update is available.
	 */
	public @Nullable Update update() {
		return this.update;
	}

	public @NonNull String@NonNull [] getModules() {
		return this.modules;
	}

	@Override
	public @NonNull String rawLocale() throws NamelessException {
		return this.rawLanguage;
	}

	public static class Update {

		private final boolean isUrgent;
		private final String version;

		Update(final boolean isUrgent, final String version) {
			this.isUrgent = isUrgent;
			this.version = version;
		}

		public boolean isUrgent() {
			return this.isUrgent;
		}

		public String rawVersion() {
			return this.version;
		}

		public @Nullable NamelessVersion parsedVersion() {
			return NamelessVersion.parse(this.version);
		}

		public @Nullable NamelessVersion getParsedVersion() {
			return NamelessVersion.parse(this.version);
		}

	}

}
