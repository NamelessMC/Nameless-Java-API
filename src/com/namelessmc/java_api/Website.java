package com.namelessmc.java_api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class Website implements LanguageEntity {


	private final @NonNull String version;
	private final @Nullable Update update;
	private final @NonNull String@NonNull[] modules;
	private final @NonNull String language;

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

		this.language = json.get("language").getAsString();
	}

	public @NonNull String getVersion() {
		return this.version;
	}

	public @NonNull NamelessVersion getParsedVersion() throws UnknownNamelessVersionException {
		return NamelessVersion.parse(this.version);
	}

	/**
	 * @return Information about an update, or empty if no update is available.
	 */
	public @NonNull Optional<@NonNull Update> getUpdate() {
		return Optional.ofNullable(this.update);
	}

	public @NonNull String@NonNull [] getModules() {
		return this.modules;
	}

	@Override
	public @NonNull String getLanguage() {
		return this.language;
	}

	/**
	 * Get POSIX code for website language (uses lookup table)
	 * @return Language code or null if the website's language does not exist in our lookup table
	 */
	@Override
	public @Nullable String getLanguagePosix() {
		return LanguageCodeMap.getLanguagePosix(this.language);
	}

	public static class Update {

		private final boolean isUrgent;
		private final @NonNull String version;

		Update(final boolean isUrgent, final @NonNull String version) {
			this.isUrgent = isUrgent;
			this.version = version;
		}

		public boolean isUrgent() {
			return this.isUrgent;
		}


		public @NonNull String getVersion() {
			return this.version;
		}

		public NamelessVersion getParsedVersion() throws UnknownNamelessVersionException {
			return NamelessVersion.parse(this.version);
		}

	}

}
