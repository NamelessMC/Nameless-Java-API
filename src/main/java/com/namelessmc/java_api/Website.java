package main.java.com.namelessmc.java_api;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.java.com.namelessmc.java_api.exception.UnknownNamelessVersionException;

public class Website {

	private final String version;
	private final Optional<Update> update;
	private final String[] modules;
	private final String language;

	Website(final JsonObject json) {
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
				this.update = Optional.of(new Update(isUrgent, updateVersion));
			} else {
				this.update = Optional.empty();
			}
		} else {
			this.update = Optional.empty();
		}

		this.language = json.get("language").getAsString();
	}

	public String getVersion() {
		return this.version;
	}

	public NamelessVersion getParsedVersion() throws UnknownNamelessVersionException {
		return NamelessVersion.parse(this.version);
	}

	/**
	 * @return Information about an update, or null if no update is available.
	 */
	public Optional<Update> getUpdate() {
		return this.update;
	}

	public String[] getModules() {
		return this.modules;
	}

	public String getLanguage() {
		return this.language;
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

		public String getVersion() {
			return this.version;
		}

		public NamelessVersion getParsedVersion() throws UnknownNamelessVersionException {
			return NamelessVersion.parse(this.version);
		}

	}

}
