package com.namelessmc.java_api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.NamelessException;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Website implements LanguageEntity {

	private final String version;
	private final @Nullable Update update;
	private final Set<String> modules;
	private final String rawLanguage;

	Website(final JsonObject json) throws NamelessException {
		if (!json.has("nameless_version")) {
			// This is usually the point where people run into issues if the response is not from NamelessMC
			// but from something else like a proxy or denial of service protection system, so we throw a useful
			// exception.
			throw new NamelessException("Website didn't include namelessmc_version in the info response. Is the response from NamelessMC?");
		}

		this.version = json.get("nameless_version").getAsString();

		this.modules = StreamSupport.stream(json.get("modules").getAsJsonArray().spliterator(), false)
				.map(JsonElement::getAsString)
				.collect(Collectors.toUnmodifiableSet());

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

		if (json.get("locale").isJsonNull()) {
			throw new NamelessException("Website returned null locale. This can happen if you upgraded from v2-pr12 to v2-pr13, please try switching the site's language to something else and back.");
		}

		this.rawLanguage = json.get("locale").getAsString();
	}

	public String rawVersion() {
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

	public Set<String> modules() {
		return this.modules;
	}

	@Override
	public String rawLocale() throws NamelessException {
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
