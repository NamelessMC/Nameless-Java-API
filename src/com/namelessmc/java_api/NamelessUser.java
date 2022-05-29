package com.namelessmc.java_api;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.Notification.Type;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.integrations.*;
import com.namelessmc.java_api.modules.store.StoreUser;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class NamelessUser implements LanguageEntity {


	private final @NonNull NamelessAPI api;
	private final @NonNull RequestHandler requests;

	private int id; // -1 if not known
	private String userTransformer;

	// Do not use directly, instead use userInfo() and integrations()
	private @Nullable JsonObject _cachedUserInfo;
	private @Nullable Map<String, DetailedIntegrationData> _cachedIntegrationData;


	NamelessUser(final @NonNull NamelessAPI api,
				 final @Positive int id
	) {
		this.api = api;
		this.requests = api.getRequestHandler();

		this.id = id;
		this.userTransformer = "id:" + id;
	}

	NamelessUser(final @NonNull NamelessAPI api, final @NonNull String userTransformer) {
		this.api = api;
		this.requests = api.getRequestHandler();

		this.id = -1;
		this.userTransformer = URLEncoder.encode(userTransformer, StandardCharsets.UTF_8);
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

	public int getId() throws NamelessException {
		if (this.id == -1) {
			this.id = this.userInfo().get("id").getAsInt();
		}

		return this.id;
	}

	public @NonNull String username() throws NamelessException {
		return this.userInfo().get("username").getAsString();
	}

	public void updateUsername(final @NonNull String username) throws NamelessException {
		JsonObject post = new JsonObject();
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
		final boolean verified = isVerified();
		final JsonObject verification = this.userInfo().getAsJsonObject("verification");
		return new VerificationInfo(verified, verification);
	}

	/**
	 * @return True if the user is member of at least one staff group, otherwise false
	 */
	public boolean isStaff() throws NamelessException {
		JsonArray groups = this.userInfo().getAsJsonArray("groups");
		for (JsonElement elem : groups) {
			JsonObject group = elem.getAsJsonObject();
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
		// TODO sorting may be unnecessary since the website already returns sorted groups
		return Collections.unmodifiableList(
				StreamSupport.stream(this.userInfo().getAsJsonArray("groups").spliterator(), false)
						.map(JsonElement::getAsJsonObject)
						.map(Group::new)
						.sorted()
						.collect(Collectors.toList()));
	}

	/**
	 * Same as doing {@link #groups()}.get(0), but with better performance
	 * since it doesn't need to create and sort a list of group objects.
	 * Empty if the user is not in any groups.
	 *
	 * @return Player's group with the lowest order
	 */
	public @Nullable Group primaryGroup() throws NamelessException {
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
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post("users/" + this.userTransformer + "/groups/add", post);
		invalidateCache(); // Groups modified, invalidate cache
	}

	public void removeGroups(final @NonNull Group@NonNull... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post("users/" + this.userTransformer + "/groups/remove", post);
		invalidateCache(); // Groups modified, invalidate cache
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

		final List<Notification> notifications = new ArrayList<>();
		response.getAsJsonArray("notifications").forEach((element) -> {
			final String message = element.getAsJsonObject().get("message").getAsString();
			final String url = element.getAsJsonObject().get("url").getAsString();
			final Type type = Type.fromString(element.getAsJsonObject().get("type").getAsString());
			notifications.add(new Notification(message, url, type));
		});

		return notifications;
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
		post.addProperty("reporter", this.getId());
		post.addProperty("reported", user.getId());
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
		Objects.requireNonNull(reportedUuid, "Reported uuid is null");
		Objects.requireNonNull(reportedName, "Reported name is null");
		Objects.requireNonNull(reason, "Report reason is null");
		Preconditions.checkArgument(reason.length() < 255,
				"Report reason too long, it's %s characters but must be less than 255", reason.length());
		final JsonObject post = new JsonObject();
		post.addProperty("reporter", this.getId());
		post.addProperty("reported_uid", reportedUuid.toString());
		post.addProperty("reported_username", reportedName);
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

	public void discordRoles(final long@NonNull[] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.getId());
		post.add("roles", this.requests.gson().toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}

	/**
	 * Get announcements visible to this user
	 * @return List of announcements visible to this user
	 */
	public @NonNull List<@NonNull Announcement> announcements() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.userTransformer + "/announcements");
		return NamelessAPI.getAnnouncements(response);
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
			int id = Integer.parseInt(e.getKey());
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
					values.get("value").getAsString()
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
		Map<String, DetailedIntegrationData> integrationDataMap = new HashMap<>(integrationsJsonArray.size());
		for (JsonElement integrationElement : integrationsJsonArray) {
			JsonObject integrationJson = integrationElement.getAsJsonObject();
			String integrationName = integrationJson.get("integration").getAsString();
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
		final DetailedIntegrationData integration = this.integrations().get(StandardIntegrationTypes.MINECRAFT);
		if (integration == null) {
			return null;
		}

		return ((IMinecraftIntegrationData) integration).uuid();
	}

	public @Nullable Long discordId() throws NamelessException {
		final DetailedIntegrationData integration = this.integrations().get(StandardIntegrationTypes.DISCORD);

		if (integration == null) {
			return null;
		}

		return ((IDiscordIntegrationData) integration).idLong();
	}

	public void verify(final @NonNull String verificationCode) throws NamelessException {
		final JsonObject body = new JsonObject();
		body.addProperty("code", verificationCode);
		this.requests.post("users/" + this.userTransformer + "/verify", body);
	}

	public StoreUser store() {
		return new StoreUser(this);
	}

}
