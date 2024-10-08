package com.namelessmc.java_api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.integrations.DetailedDiscordIntegrationData;
import com.namelessmc.java_api.integrations.DetailedIntegrationData;
import com.namelessmc.java_api.integrations.DetailedMinecraftIntegrationData;
import com.namelessmc.java_api.integrations.IDiscordIntegrationData;
import com.namelessmc.java_api.integrations.IMinecraftIntegrationData;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.integrations.StandardIntegrationTypes;
import com.namelessmc.java_api.modules.discord.DiscordUser;
import com.namelessmc.java_api.modules.store.StoreUser;
import com.namelessmc.java_api.modules.suggestions.SuggestionsUser;
import com.namelessmc.java_api.util.GsonHelper;

public final class NamelessUser implements LanguageEntity {

	private final @NonNull NamelessAPI api;
	private final @NonNull RequestHandler requests;

	private int id; // -1 if not known
	private String userTransformer;

	// Do not use directly, instead use userInfo() and integrations()
	private @Nullable JsonObject _cachedUserInfo;
	private @Nullable Map<String, DetailedIntegrationData> _cachedIntegrationData;


	NamelessUser(final @NonNull NamelessAPI api, final @Positive int id) {
		this.api = api;
		this.requests = api.requests();

		this.id = id;
		this.userTransformer = "id:" + id;
	}

	NamelessUser(final @NonNull NamelessAPI api, final @NonNull String userTransformer) {
		this.api = api;
		this.requests = api.requests();

		this.id = -1;
		this.userTransformer = URLEncoder.encode(userTransformer, StandardCharsets.UTF_8);
	}

	NamelessUser(final NamelessAPI api, final JsonObject userInfo) {
		this(api, userInfo.get("id").getAsInt());
		this._cachedUserInfo = userInfo;
	}

	@NonNull JsonObject userInfo() throws NamelessException {
		if (this._cachedUserInfo != null) {
			return this._cachedUserInfo;
		}

		final JsonObject response = this.requests.get("users/" + this.userTransformer);

		if (!response.get("exists").getAsBoolean()) {
			throw new IllegalStateException("User was returned by the API without an error code so it should exist");
		}

		this._cachedUserInfo = response;

		if (this.id < 0) {
			// The id was unknown before (we were using some other identifier to find the user)
			// Now that we do know the id, use the id to identify the user instead
			this.id = response.get("id").getAsInt();
			this.userTransformer = "id:" + this.id;
		}

		return response;
	}

	public @NonNull NamelessAPI api() {
		return this.api;
	}

	/**
	 * The API method `userInfo` is only called once to improve performance.
	 * This means that if something changes on the website, methods that use
	 * data from the `userInfo` API method will keep returning the old data.
	 * Calling this method will invalidate the cache and require making a new
	 * API request. It will not make a new API request immediately. Calling
	 * this method multiple times while the cache is already cleared has no
	 * effect.
	 */
	public void invalidateCache() {
		this._cachedUserInfo = null;
		this._cachedIntegrationData = null;
	}

	public String userTransformer() {
		return this.userTransformer;
	}

	public int id() throws NamelessException {
		if (this.id == -1) {
			this.id = this.userInfo().get("id").getAsInt();
		}

		return this.id;
	}

	public @NonNull String username() throws NamelessException {
		return this.userInfo().get("username").getAsString();
	}

	public void updateUsername(final @NonNull String username) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		this.requests.post("users/" + this.userTransformer + "/update-username", post);
	}

	public @NonNull String displayName() throws NamelessException {
		return this.userInfo().get("displayname").getAsString();
	}

	/**
	 * @return The date the user registered on the website.
	 */
	public @NonNull Date registeredDate() throws NamelessException {
		return new Date(this.userInfo().get("registered_timestamp").getAsLong() * 1000);
	}

	public @NonNull Date lastOnline() throws NamelessException {
		return new Date(this.userInfo().get("last_online_timestamp").getAsLong() * 1000);
	}

	/**
	 * @return Whether this account is banned from the website.
	 */
	public boolean isBanned() throws NamelessException {
		return this.userInfo().get("banned").getAsBoolean();
	}

	public boolean isVerified() throws NamelessException {
		return this.userInfo().get("validated").getAsBoolean();
	}

	@Override
	public @NonNull String rawLocale() throws NamelessException {
		return this.userInfo().get("locale").getAsString();
	}

	public @NonNull VerificationInfo verificationInfo() throws NamelessException {
		final boolean verified = this.isVerified();
		final JsonObject verification = this.userInfo().getAsJsonObject("verification");
		return new VerificationInfo(verified, verification);
	}

	/**
	 * @return True if the user is member of at least one staff group, otherwise false
	 */
	public boolean isStaff() throws NamelessException {
		if (!this.userInfo().has("groups")) {
			throw new IllegalStateException("Groups array missing: https://github.com/NamelessMC/Nameless/issues/3052");
		}

		final JsonArray groups = this.userInfo().getAsJsonArray("groups");
		for (final JsonElement elem : groups) {
			final JsonObject group = elem.getAsJsonObject();
			if (group.has("staff") &&
					group.get("staff").getAsBoolean()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return List of the user's groups, sorted from low order to high order.
	 */
	public @NonNull List<@NonNull Group> groups() throws NamelessException {
		if (!this.userInfo().has("groups")) {
			throw new IllegalStateException("Groups array missing: https://github.com/NamelessMC/Nameless/issues/3052");
		}
		return GsonHelper.toObjectList(this.userInfo().getAsJsonArray("groups"), Group::new);
	}

	/**
	 * Same as doing {@link #groups()}.get(0), but with better performance
	 * since it doesn't need to create and sort a list of group objects.
	 * Empty if the user is not in any groups.
	 *
	 * @return Player's group with the lowest order
	 */
	public @Nullable Group primaryGroup() throws NamelessException {
		if (!this.userInfo().has("groups")) {
			throw new IllegalStateException("Groups array missing: https://github.com/NamelessMC/Nameless/issues/3052");
		}
		final JsonArray groups = this.userInfo().getAsJsonArray("groups");
		if (groups.size() > 0) {
			// Website group response is ordered, first group is primary group.
			return new Group(groups.get(0).getAsJsonObject());
		} else {
			return null;
		}
	}

	public void addGroups(final @NonNull Group@NonNull ... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("groups", this.groupsToJsonArray(groups));
		this.requests.post("users/" + this.userTransformer + "/groups/add", post);
		this.invalidateCache(); // Groups modified, invalidate cache
	}

	public void removeGroups(final @NonNull Group@NonNull... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("groups", this.groupsToJsonArray(groups));
		this.requests.post("users/" + this.userTransformer + "/groups/remove", post);
		this.invalidateCache(); // Groups modified, invalidate cache
	}
	
	public void updateMinecraftGroups(final String[] addedGroups, final String[] removedGroups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("add", this.requests.gson().toJsonTree(addedGroups));
		post.add("remove", this.requests.gson().toJsonTree(removedGroups));
		this.requests.post("minecraft/" + this.userTransformer + "/sync-groups", post);
	}

	private JsonArray groupsToJsonArray(final @NonNull Group@NonNull [] groups) {
		final JsonArray array = new JsonArray();
		for (final Group group : groups) {
			array.add(group.getId());
		}
		return array;
	}

	public int notificationCount() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.userTransformer + "/notifications");
		return response.getAsJsonArray("notifications").size();
	}

	public List<Notification> notifications() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.userTransformer + "/notifications");
		return GsonHelper.toObjectList(response.getAsJsonArray("notifications"), Notification::new);
	}

	/**
	 * Creates a report for a website user
	 * @param user User to report. Lazy loading possible, only the ID is used.
	 * @param reason Reason why this player has been reported
	 * @throws IllegalArgumentException Report reason is too long (>255 characters)
	 * @throws IllegalArgumentException Report reason is too long (>255 characters)
	 */
	public void createReport(final @NonNull NamelessUser user, final @NonNull String reason) throws NamelessException {
		Objects.requireNonNull(user, "User to report is null");
		Objects.requireNonNull(reason, "Report reason is null");
		Preconditions.checkArgument(reason.length() < 255,
				"Report reason too long, it's %s characters but must be less than 255", reason.length());
		final JsonObject post = new JsonObject();
		post.addProperty("reporter", this.id());
		post.addProperty("reported", user.id());
		post.addProperty("content", reason);
		try {
			this.requests.post("reports/create", post);
		} catch (final ApiException e) {
			if (e.apiError() == ApiError.CORE_REPORT_CONTENT_TOO_LONG) {
				throw new IllegalStateException("Website said report reason is too long, but we have " +
						"client-side validation for this so it should be impossible");
			}
			throw e;
		}
	}

	/**
	 * Create a report for a user who may or may not have a website account
	 * @param reportedUuid The Mojang UUID of the Minecraft player to report
	 * @param reportedName The Minecraft username of this player
	 * @param reason Report reason
	 * @throws IllegalArgumentException Report reason is too long (>255 characters)
	 */
	public void createReport(final @NonNull UUID reportedUuid,
							 final @NonNull String reportedName,
							 final @NonNull String reason) throws NamelessException {
		this.createReport(reportedUuid, reportedName, reason, 0);
	}
	
	/**
	 * Create a report for a user who may or may not have a website account
	 * @param reportedUuid The Mojang UUID of the Minecraft player to report
	 * @param reportedName The Minecraft username of this player
	 * @param reason Report reason
	 * @param serverId Minecraft server id
	 * @throws IllegalArgumentException Report reason is too long (>255 characters)
	 */
	public void createReport(final @NonNull UUID reportedUuid,
							 final @NonNull String reportedName,
							 final @NonNull String reason,
							 final int serverId) throws NamelessException {
		Objects.requireNonNull(reportedUuid, "Reported uuid is null");
		Objects.requireNonNull(reportedName, "Reported name is null");
		Objects.requireNonNull(reason, "Report reason is null");
		Preconditions.checkArgument(reason.length() < 255,
				"Report reason too long, it's %s characters but must be less than 255", reason.length());
		final JsonObject post = new JsonObject();
		post.addProperty("reporter", this.id());
		post.addProperty("reported_uid", reportedUuid.toString());
		post.addProperty("reported_username", reportedName);
		post.addProperty("content", reason);
		if (serverId != 0) {
			post.addProperty("server_id", serverId);
		}
		try {
			this.requests.post("reports/create", post);
		} catch (final ApiException e) {
			if (e.apiError() == ApiError.CORE_REPORT_CONTENT_TOO_LONG) {
				throw new IllegalStateException("Website said report reason is too long, but we have " +
						"client-side validation for this so it should be impossible");
			}
			throw e;
		}
	}

	/**
	 * Get announcements visible to this user
	 * @return List of announcements visible to this user
	 */
	public @NonNull List<@NonNull Announcement> announcements() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.userTransformer + "/announcements");
		return NamelessAPI.announcements(response);
	}

	/**
	 * Ban this user
	 * @since 2021-10-24 commit <code>cce8d262b0be3f70818c188725cd7e7fc4fdbb9a</code>
	 */
	public void banUser() throws NamelessException {
		this.requests.post("users/" + this.userTransformer + "/ban", new JsonObject());
	}

	public Collection<CustomProfileFieldValue> profileFields() throws NamelessException {
		if (!this.userInfo().has("profile_fields")) {
			return Collections.emptyList();
		}

		final JsonObject fieldsJson = this.userInfo().getAsJsonObject("profile_fields");
		final List<CustomProfileFieldValue> fieldValues = new ArrayList<>(fieldsJson.size());
		for (final Map.Entry<String, JsonElement> e : fieldsJson.entrySet()) {
			final int id = Integer.parseInt(e.getKey());
			final JsonObject values = e.getValue().getAsJsonObject();
			fieldValues.add(new CustomProfileFieldValue(
					new CustomProfileField(
							id,
							values.get("name").getAsString(),
							CustomProfileFieldType.fromNamelessTypeInt(values.get("type").getAsInt()),
							values.get("public").getAsBoolean(),
							values.get("required").getAsBoolean(),
							values.get("description").getAsString()
					),
					GsonHelper.getNullableString(values, "value")
			));
		}

		return fieldValues;
	}

	public Map<String, DetailedIntegrationData> integrations() throws NamelessException {
		if (this._cachedIntegrationData != null) {
			return this._cachedIntegrationData;
		}

		final JsonObject userInfo = this.userInfo();
		final JsonArray integrationsJsonArray = userInfo.getAsJsonArray("integrations");
		final Map<String, DetailedIntegrationData> integrationDataMap = new HashMap<>(integrationsJsonArray.size());
		for (final JsonElement integrationElement : integrationsJsonArray) {
			final JsonObject integrationJson = integrationElement.getAsJsonObject();
			final String integrationName = integrationJson.get("integration").getAsString();
			DetailedIntegrationData integrationData;
			switch(integrationName) {
				case StandardIntegrationTypes.MINECRAFT:
					integrationData = new DetailedMinecraftIntegrationData(integrationJson);
					break;
				case StandardIntegrationTypes.DISCORD:
					integrationData = new DetailedDiscordIntegrationData(integrationJson);
					break;
				default:
					integrationData = new DetailedIntegrationData(integrationJson);
			}
			integrationDataMap.put(integrationName, integrationData);
		}
		this._cachedIntegrationData = integrationDataMap;
		return integrationDataMap;
	}

	public @Nullable UUID minecraftUuid() throws NamelessException {
		final IntegrationData integration = this.integrations().get(StandardIntegrationTypes.MINECRAFT);
		return integration == null ? null : ((IMinecraftIntegrationData) integration).uuid();
	}

	public @Nullable String minecraftUsername() throws NamelessException {
		final IntegrationData integration = this.integrations().get(StandardIntegrationTypes.MINECRAFT);
		return integration == null ? null : integration.username();
	}

	public @Nullable Long discordId() throws NamelessException {
		final IntegrationData integration = this.integrations().get(StandardIntegrationTypes.DISCORD);
		return integration == null ? null : ((IDiscordIntegrationData) integration).idLong();
	}

	public @Nullable String discordUsername() throws NamelessException {
		final IntegrationData integration = this.integrations().get(StandardIntegrationTypes.DISCORD);
		return integration == null ? null : integration.username();
	}

	public void verify(final @NonNull String verificationCode) throws NamelessException {
		final JsonObject body = new JsonObject();
		body.addProperty("code", verificationCode);
		this.requests.post("users/" + this.userTransformer + "/verify", body);
	}

	public DiscordUser discord() throws NamelessException {
		return new DiscordUser(this);
	}

	public StoreUser store() throws NamelessException {
		return new StoreUser(this);
	}

	public SuggestionsUser suggestions() throws NamelessException {
		return new SuggestionsUser(this);
	}

}
