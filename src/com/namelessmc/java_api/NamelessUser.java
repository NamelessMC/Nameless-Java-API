package com.namelessmc.java_api;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.Notification.NotificationType;
import com.namelessmc.java_api.exception.*;
import com.namelessmc.java_api.integrations.*;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class NamelessUser implements LanguageEntity {


	private final @NonNull NamelessAPI api;
	private final @NonNull RequestHandler requests;

	private @NonNull String userTransformer;
	private int id; // -1 if not known

	// Do not use directly, instead use getUserInfo() and getIntegrations()
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
		this.userTransformer = userTransformer;
	}

	private @NonNull JsonObject getUserInfo() throws NamelessException {
		if (this._cachedUserInfo != null) {
			return this._cachedUserInfo;
		}

		final JsonObject response;
		try {
			response = this.requests.get("users/" + this.userTransformer);
		} catch (final ApiError e) {
			if (e.getError() == ApiError.UNABLE_TO_FIND_USER) {
				throw new UserNotExistException();
			} else {
				throw e;
			}
		}

		if (!response.get("exists").getAsBoolean()) {
			throw new UserNotExistException();
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

	public @NonNull NamelessAPI getApi() {
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

	public int getId() throws NamelessException {
		if (this.id == -1) {
			this.id = this.getUserInfo().get("id").getAsInt();
		}

		return this.id;
	}

	public @NonNull String getUsername() throws NamelessException {
		return this.getUserInfo().get("username").getAsString();
	}

	public void updateUsername(final @NonNull String username) throws NamelessException {
		JsonObject post = new JsonObject();
		post.addProperty("username", username);
		this.requests.post("users/" + this.userTransformer + "/update-username", post);
	}

	public boolean exists() throws NamelessException {
		try {
			this.getUserInfo();
			return true;
		} catch (final UserNotExistException e) {
			return false;
		}
	}

	public @NonNull String getDisplayName() throws NamelessException {
		return this.getUserInfo().get("displayname").getAsString();
	}

	/**
	 * @return The date the user registered on the website.
	 */
	public @NonNull Date getRegisteredDate() throws NamelessException {
		return new Date(this.getUserInfo().get("registered_timestamp").getAsLong() * 1000);
	}

	public @NonNull Date getLastOnline() throws NamelessException {
		return new Date(this.getUserInfo().get("last_online_timestamp").getAsLong() * 1000);
	}

	/**
	 * @return Whether this account is banned from the website.
	 */
	public boolean isBanned() throws NamelessException {
		return this.getUserInfo().get("banned").getAsBoolean();
	}

	public boolean isVerified() throws NamelessException {
		return this.getUserInfo().get("validated").getAsBoolean();
	}

	@Override
	public @NonNull String getRawLocale() throws NamelessException {
		return this.getUserInfo().get("locale").getAsString();
	}

	public @NonNull VerificationInfo getVerificationInfo() throws NamelessException {
		final boolean verified = isVerified();
		final JsonObject verification = this.getUserInfo().getAsJsonObject("verification");
		return new VerificationInfo(verified, verification);
	}

	/**
	 * @return True if the user is member of at least one staff group, otherwise false
	 */
	public boolean isStaff() throws NamelessException {
		JsonArray groups = this.getUserInfo().getAsJsonArray("groups");
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
	 * @return Set of user's groups
	 * @see #getSortedGroups()
	 */
	public @NonNull Set<@NonNull Group> getGroups() throws NamelessException {
		return Collections.unmodifiableSet(
				StreamSupport.stream(this.getUserInfo().getAsJsonArray("groups").spliterator(), false)
						.map(JsonElement::getAsJsonObject)
						.map(Group::new)
						.collect(Collectors.toSet()));
	}

	/**
	 * @return List of the user's groups, sorted from low order to high order.
	 * @see #getGroups()
	 */
	public @NonNull List<@NonNull Group> getSortedGroups() throws NamelessException {
		return Collections.unmodifiableList(
				StreamSupport.stream(this.getUserInfo().getAsJsonArray("groups").spliterator(), false)
						.map(JsonElement::getAsJsonObject)
						.map(Group::new)
						.sorted()
						.collect(Collectors.toList()));
	}

	/**
	 * Same as doing {@link #getGroups()}.get(0), but with better performance
	 * since it doesn't need to create and sort a list of group objects.
	 * Empty if the user is not in any groups.
	 *
	 * @return Player's group with the lowest order
	 */
	public @Nullable Group getPrimaryGroup() throws NamelessException {
		final JsonArray groups = this.getUserInfo().getAsJsonArray("groups");
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

	public int getNotificationCount() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.userTransformer + "/notifications");
		return response.getAsJsonArray("notifications").size();
	}

	public @NonNull List<Notification> getNotifications() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.userTransformer + "/notifications");

		final List<Notification> notifications = new ArrayList<>();
		response.getAsJsonArray("notifications").forEach((element) -> {
			final String message = element.getAsJsonObject().get("message").getAsString();
			final String url = element.getAsJsonObject().get("url").getAsString();
			final NotificationType type = NotificationType.fromString(element.getAsJsonObject().get("type").getAsString());
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
	 * @throws NamelessException Unexpected http or api error
	 * @throws ReportUserBannedException If the user creating this report is banned
	 * @throws AlreadyHasOpenReportException If the user creating this report already has an open report for this user
	 * @throws CannotReportSelfException If the user tries to report themselves
	 */
	public void createReport(final @NonNull NamelessUser user, final @NonNull String reason)
			throws NamelessException, ReportUserBannedException, AlreadyHasOpenReportException, CannotReportSelfException {
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
		} catch (final ApiError e) {
			switch (e.getError()) {
				case ApiError.USER_CREATING_REPORT_BANNED: throw new ReportUserBannedException();
				case ApiError.REPORT_CONTENT_TOO_LARGE:
					throw new IllegalStateException("Website said report reason is too long, but we have " +
							"client-side validation for this so it should be impossible");
				case ApiError.USER_ALREADY_HAS_OPEN_REPORT: throw new AlreadyHasOpenReportException();
				case ApiError.CANNOT_REPORT_YOURSELF: throw new CannotReportSelfException();
				default: throw e;
			}
		}
	}

	/**
	 * Create a report for a user who may or may not have a website account
	 * @param reportedUuid The Mojang UUID of the Minecraft player to report
	 * @param reportedName The Minecraft username of this player
	 * @param reason Report reason
	 * @throws IllegalArgumentException Report reason is too long (>255 characters)
	 * @throws NamelessException Unexpected http or api error
	 * @throws ReportUserBannedException If the user creating this report is banned
	 * @throws AlreadyHasOpenReportException If the user creating this report already has an open report for this user
	 * @throws CannotReportSelfException If the user tries to report themselves
	 */
	public void createReport(final @NonNull UUID reportedUuid,
							 final @NonNull String reportedName,
							 final @NonNull String reason)
			throws NamelessException, ReportUserBannedException, AlreadyHasOpenReportException, CannotReportSelfException {
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
		} catch (final ApiError e) {
			switch (e.getError()) {
				case ApiError.USER_CREATING_REPORT_BANNED: throw new ReportUserBannedException();
				case ApiError.REPORT_CONTENT_TOO_LARGE:
					throw new IllegalStateException("Website said report reason is too long, but we have " +
							"client-side validation for this so it should be impossible");
				case ApiError.USER_ALREADY_HAS_OPEN_REPORT: throw new AlreadyHasOpenReportException();
				case ApiError.CANNOT_REPORT_YOURSELF: throw new CannotReportSelfException();
				default: throw e;
			}
		}
	}

	public void setDiscordRoles(final long@NonNull[] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.getId());
		post.add("roles", this.requests.gson().toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}

	/**
	 * Get announcements visible to this user
	 * @return List of announcements visible to this user
	 */
	public @NonNull List<@NonNull Announcement> getAnnouncements() throws NamelessException {
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

	public @NonNull Collection<@NonNull CustomProfileFieldValue> getProfileFields() throws NamelessException {
		if (!this.getUserInfo().has("profile_fields")) {
			return Collections.emptyList();
		}

		final JsonObject fieldsJson = this.getUserInfo().getAsJsonObject("profile_fields");
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

	public Map<String, DetailedIntegrationData> getIntegrations() throws NamelessException {
		if (this._cachedIntegrationData != null) {
			return this._cachedIntegrationData;
		}

		final JsonObject userInfo = this.getUserInfo();
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

	public @Nullable UUID getMinecraftUuid() throws NamelessException {
		final DetailedIntegrationData integration = this.getIntegrations().get(StandardIntegrationTypes.MINECRAFT);
		if (integration == null) {
			return null;
		}

		return ((IMinecraftIntegrationData) integration).getUniqueId();
	}

	public @Nullable Long getDiscordId() throws NamelessException {
		final DetailedIntegrationData integration = this.getIntegrations().get(StandardIntegrationTypes.DISCORD);

		if (integration == null) {
			return null;
		}

		return ((IDiscordIntegrationData) integration).getIdLong();
	}

	public void verify(final @NonNull String verificationCode) throws NamelessException, AccountAlreadyActivatedException, InvalidValidateCodeException {
		final JsonObject body = new JsonObject();
		body.addProperty("code", verificationCode);
		try {
			this.requests.post("users/" + this.userTransformer + "/verify", body);
		} catch (final ApiError e) {
			switch(e.getError()) {
				case ApiError.ACCOUNT_ALREADY_ACTIVATED:
					throw new AccountAlreadyActivatedException();
				case ApiError.INVALID_VALIDATE_CODE:
					throw new InvalidValidateCodeException();
				default:
					throw e;
			}
		}
	}

}
