package com.namelessmc.java_api;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.Notification.NotificationType;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.CannotReportSelfException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class NamelessUser {

	@NotNull
	private final NamelessAPI api;
	@NotNull
	private final RequestHandler requests;

	private int id; // -1 if not known
	private @Nullable String username; // null if not known
	private boolean uuidKnown;
	private @Nullable UUID uuid; // null if not known or not present
	private boolean discordIdKnown;
	private long discordId; // -1 if not known or not present

	@Nullable
	private JsonObject userInfo;

	/**
	 * Create a Nameless user. Only one of 'id', 'uuid', 'discordId' has to be provided.
	 * @param api Nameless API
	 * @param id The user's id, or -1 if not known
	 * @param username The user's username, or null if not known
	 * @param uuidKnown True if it is known whether this user has a UUID or not
	 * @param uuid The user's uuid, or null if the user doesn't have a UUID, or it is not known whether the user has a UUID
	 * @param discordIdKnown True if it is known whether this user has a linked Discord id or not
	 * @param discordId The user's discord id, or -1 if the user doesn't have a linked Discord id, or it is not known whether the user has a Discord id
	 */
	NamelessUser(@NotNull final NamelessAPI api,
				 final int id,
				 @Nullable final String username,
				 boolean uuidKnown,
				 @Nullable UUID uuid,
				 boolean discordIdKnown,
				 long discordId
	) {
		this.api = api;
		this.requests = api.getRequestHandler();

		if (id == -1 && username == null && !uuidKnown && !discordIdKnown) {
			throw new IllegalArgumentException("You must specify at least one of ID, uuid, username, discordId");
		}

		this.id = id;
		this.username = username;
		this.uuidKnown = uuidKnown;
		this.uuid = uuid;
		this.discordIdKnown = discordIdKnown;
		this.discordId = discordId;
	}

	private void loadUserInfo() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.getUserTransformer());

		if (!response.get("exists").getAsBoolean()) {
			throw new UserNotExistException();
		}

		this.userInfo = response;
	}

	public String getUserTransformer() {
		if (id != 0) {
			return "id:" + this.id;
		} else if (this.uuidKnown && this.uuid != null) {
			return "integration_id:minecraft:" + this.uuid;
		} else if (this.discordIdKnown && this.discordId != 0) {
			return "integration_id:discord:" + this.discordId;
		} else if (this.username != null) {
			return "username:" + username;
		} else {
			throw new IllegalStateException("ID, uuid, and username not known for this player. " +
					"This should be impossible, the constructor checks for this.");
		}
	}

	@NotNull
	public NamelessAPI getApi() {
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
		this.userInfo = null;
	}

	public int getId() throws NamelessException {
		if (this.id == -1) {
			this.loadUserInfo();
			//noinspection ConstantConditions
			this.id = this.userInfo.get("id").getAsInt();
		}

		return this.id;
	}

	public @NotNull String getUsername() throws NamelessException {
		if (this.username == null) {
			this.loadUserInfo();
			//noinspection ConstantConditions
			this.username = this.userInfo.get("username").getAsString();
		}

		return this.username;
	}

	public void updateUsername(final @NotNull String username) throws NamelessException {
		JsonObject post = new JsonObject();
		post.addProperty("username", username);
		this.requests.post("users/" + this.getUserTransformer() + "/update-username", post);
	}

	public @NotNull Optional<@NotNull UUID> getUniqueId() throws NamelessException {
		if (!this.uuidKnown) {
			this.loadUserInfo();
			//noinspection ConstantConditions
			if (this.userInfo.has("uuid")) {
				final String uuidString = this.userInfo.get("uuid").getAsString();
				if (uuidString == null ||
						uuidString.equals("none") ||
						uuidString.equals("")) {
					this.uuid = null;
				} else {
					this.uuid = NamelessAPI.websiteUuidToJavaUuid(uuidString);
				}
			} else {
				this.uuid = null;
			}
			this.uuidKnown = true;
		}

		return Optional.ofNullable(this.uuid);
	}

	public @NotNull Optional<@NotNull Long> getDiscordId() throws NamelessException {
		if (!this.discordIdKnown) {
			this.loadUserInfo();
			//noinspection ConstantConditions
			if (this.userInfo.has("discord_id")) {
				this.discordId = this.userInfo.get("discord_id").getAsLong();
			} else {
				this.discordId = -1;
			}
			this.discordIdKnown = true;
		}

		return this.discordId > 0 ? Optional.of(this.discordId) : Optional.empty();
	}

	public boolean exists() throws NamelessException {
		if (this.userInfo == null) {
			try {
				loadUserInfo();
			} catch (final UserNotExistException e) {
				return false;
			}
		}

		return true;
	}

	public @NotNull String getDisplayName() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return this.userInfo.get("displayname").getAsString();
	}

	/**
	 * @return The date the user registered on the website.
	 */
	public @NotNull Date getRegisteredDate() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return new Date(this.userInfo.get("registered_timestamp").getAsLong() * 1000);
	}

	public @NotNull Date getLastOnline() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return new Date(this.userInfo.get("last_online_timestamp").getAsLong() * 1000);
	}

	/**
	 * @return Whether this account is banned from the website.
	 */
	public boolean isBanned() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return this.userInfo.get("banned").getAsBoolean();
	}

	public boolean isVerified() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return this.userInfo.get("validated").getAsBoolean();
	}

	public @NotNull String getLanguage() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return this.userInfo.get("language").getAsString();
	}

	public @NotNull VerificationInfo getVerificationInfo() throws NamelessException {
		final boolean verified = isVerified();
		//noinspection ConstantConditions
		final JsonObject verification = this.userInfo.getAsJsonObject("verification");
		return new VerificationInfo(verified, verification);
	}

	/**
	 * @return True if the user is member of at least one staff group, otherwise false
	 */
	public boolean isStaff() throws NamelessException {
		for (final Group group : this.getGroups()) {
			if (group.isStaff()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Set of user's groups
	 * @see #getSortedGroups()
	 */
	public @NotNull Set<@NotNull Group> getGroups() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return Collections.unmodifiableSet(
				StreamSupport.stream(this.userInfo.getAsJsonArray("groups").spliterator(), false)
						.map(JsonElement::getAsJsonObject)
						.map(Group::new)
						.collect(Collectors.toSet()));
	}

	/**
	 * @return List of the user's groups, sorted from low order to high order.
	 * @see #getGroups()
	 */
	public @NotNull List<@NotNull Group> getSortedGroups() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		return Collections.unmodifiableList(
				StreamSupport.stream(this.userInfo.getAsJsonArray("groups").spliterator(), false)
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
	public @NotNull Optional<@NotNull Group> getPrimaryGroup() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		final JsonArray groups = this.userInfo.getAsJsonArray("groups");
		if (groups.size() > 0) {
			return Optional.of(new Group(groups.get(0).getAsJsonObject()));
		} else {
			return Optional.empty();
		}
	}

	public void addGroups(@NotNull final Group@NotNull ... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post("users/" + this.getUserTransformer() + "/groups/add", post);
		invalidateCache(); // Groups modified, invalidate cache
	}

	public void removeGroups(@NotNull final Group@NotNull... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post("users/" + this.getUserTransformer() + "/groups/add", post);
		invalidateCache(); // Groups modified, invalidate cache
	}

	private JsonArray groupsToJsonArray(@NotNull final Group@NotNull [] groups) {
		final JsonArray array = new JsonArray();
		for (final Group group : groups) {
			array.add(group.getId());
		}
		return array;
	}

	public int getNotificationCount() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.getUserTransformer() + "/notifications");
		return response.getAsJsonArray("notifications").size();
	}

	public @NotNull List<Notification> getNotifications() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.getUserTransformer() + "/notifications");

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
	public void createReport(@NotNull final NamelessUser user, @NotNull final String reason)
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
			if (e.getError() == ApiError.USER_CREATING_REPORT_BANNED) {
				throw new ReportUserBannedException();
			} else if (e.getError() == ApiError.REPORT_CONTENT_TOO_LARGE) {
				throw new IllegalStateException("Website said report reason is too long, but we have client-side validation for this");
			} else if (e.getError() == ApiError.USER_ALREADY_HAS_OPEN_REPORT) {
				throw new AlreadyHasOpenReportException();
			} else if (e.getError() == ApiError.CANNOT_REPORT_YOURSELF) {
				throw new CannotReportSelfException();
			} else {
				throw e;
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
	public void createReport(final @NotNull UUID reportedUuid,
							 final @NotNull String reportedName,
							 final @NotNull String reason)
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
			if (e.getError() == ApiError.USER_CREATING_REPORT_BANNED) {
				throw new ReportUserBannedException();
			} else if (e.getError() == ApiError.REPORT_CONTENT_TOO_LARGE) {
				throw new IllegalStateException("Website said report reason is too long, but we have client-side validation for this");
			} else if (e.getError() == ApiError.USER_ALREADY_HAS_OPEN_REPORT) {
				throw new AlreadyHasOpenReportException();
			} else if (e.getError() == ApiError.CANNOT_REPORT_YOURSELF) {
				throw new CannotReportSelfException();
			} else {
				throw e;
			}
		}
	}

	public void setDiscordRoles(final long@NotNull[] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.getId());
		post.add("roles", NamelessAPI.GSON.toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}

	/**
	 * Get announcements visible to this user
	 * @return List of announcements visible to this user
	 */
	@NotNull
	public List<@NotNull Announcement> getAnnouncements() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.getUserTransformer() + "/announcements");
		return NamelessAPI.getAnnouncements(response);
	}

	/**
	 * Ban this user
	 * @since 2021-10-24 commit <code>cce8d262b0be3f70818c188725cd7e7fc4fdbb9a</code>
	 */
	public void banUser() throws NamelessException {
		this.requests.post("users/" + this.getUserTransformer() + "/ban", null);
	}

	public @NotNull Collection<@NotNull CustomProfileFieldValue> getProfileFields() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}

		if (!userInfo.has("profile_fields")) {
			return Collections.emptyList();
		}

		final JsonObject fieldsJson = userInfo.getAsJsonObject("profile_fields");
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

}
