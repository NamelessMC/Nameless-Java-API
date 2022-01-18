package com.namelessmc.java_api;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.Notification.NotificationType;
import com.namelessmc.java_api.exception.AccountAlreadyActivatedException;
import com.namelessmc.java_api.exception.AlreadyHasOpenReportException;
import com.namelessmc.java_api.exception.CannotReportSelfException;
import com.namelessmc.java_api.exception.InvalidValidateCodeException;
import com.namelessmc.java_api.exception.ReportUserBannedException;
import com.namelessmc.java_api.exception.UnableToCreateReportException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
	 * @param uuidKnown True if it is known whether this user has a uuid or not
	 * @param uuid The user's uuid, or null if the user doesn't have a uuid or it is not known whether the user has a uuid
	 * @param discordIdKnown True if it is known whether this user has a linked discord id or not
	 * @param discordId The user's discord id, or -1 if the user doesn't have a linked discord id or it is not known whether the user has a discord id
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
		final JsonObject response;
		// TODO There's no way to do this right now, wait for aber
		if (this.id != -1) {
			response = this.requests.get("????", "id", this.id);
		} else if (this.uuidKnown && this.uuid != null) {
			response = this.requests.get("????", "uuid", this.uuid);
		} else if (this.username != null) {
			response = this.requests.get("????", "username", this.username);
		} else if (this.discordIdKnown && this.discordId > 0) {
			response = this.requests.get("????", "discord_id", this.discordId);
		} else {
			throw new IllegalStateException("ID, uuid, and username not known for this player.");
		}

		if (!response.get("exists").getAsBoolean()) {
			throw new UserNotExistException();
		}

		this.userInfo = response;
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

	public void updateUsername(@NotNull String username) throws NamelessException {
		JsonObject post = new JsonObject();
		post.addProperty("username", username);
		this.requests.post("users/" + this.getId() + "/update-username", post);
	}

	public @NotNull Optional<UUID> getUniqueId() throws NamelessException {
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

	public @NotNull Optional<Long> getDiscordId() throws NamelessException {
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

	public @NotNull String getLangage() throws NamelessException {
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
	 * @return Player's group with lowest order
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
		this.requests.post("users/" + this.getId() + "/groups/add", post);
		invalidateCache(); // Groups modified, invalidate cache
	}

	public void removeGroups(@NotNull final Group@NotNull... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post("users/" + this.getId() + "/groups/add", post);
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
		final JsonObject response = this.requests.get("users/" + this.getId() + "/notifications");
		return response.getAsJsonArray("notifications").size();
	}

	public @NotNull List<Notification> getNotifications() throws NamelessException {
		final JsonObject response = this.requests.get("users/" + this.getId() + "/notifications");

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
	 * @throws UnableToCreateReportException Generic error
	 * @throws CannotReportSelfException If the user tries to report themselves
	 */
	public void createReport(@NotNull final NamelessUser user, @NotNull final String reason)
			throws NamelessException, ReportUserBannedException, AlreadyHasOpenReportException, UnableToCreateReportException, CannotReportSelfException {
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
			} else if (e.getError() == ApiError.UNABLE_TO_CREATE_REPORT) {
				throw new UnableToCreateReportException();
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
	 * @throws UnableToCreateReportException Generic error
	 * @throws CannotReportSelfException If the user tries to report themselves
	 */
	public void createReport(@NotNull final UUID reportedUuid, @NotNull String reportedName, @NotNull final String reason)
			throws NamelessException, ReportUserBannedException, AlreadyHasOpenReportException, UnableToCreateReportException, CannotReportSelfException {
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
			} else if (e.getError() == ApiError.UNABLE_TO_CREATE_REPORT) {
				throw new UnableToCreateReportException();
			} else if (e.getError() == ApiError.CANNOT_REPORT_YOURSELF) {
				throw new CannotReportSelfException();
			} else {
				throw e;
			}
		}
	}

	/**
	 * Verifies a user's Minecraft account
	 * @param code Verification code
	 */
	public void verifyMinecraft(@NotNull final String code)
			throws NamelessException, InvalidValidateCodeException, AccountAlreadyActivatedException {
		Objects.requireNonNull(code, "Verification code is null");
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.getId());
		post.addProperty("code", code);
		try {
			this.requests.post("minecraft/verify", post);
			this.userInfo = null;
		} catch (final ApiError e) {
			switch (e.getError()) {
				case ApiError.INVALID_VALIDATE_CODE:
					throw new InvalidValidateCodeException();
				case ApiError.ACCOUNT_ALREADY_ACTIVATED:
					throw new AccountAlreadyActivatedException();
				default:
					throw e;
			}
		}
	}

	public void setDiscordRoles(final long[] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.getId());
		post.add("roles", NamelessAPI.GSON.toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}

	/**
	 * Ban this user
	 * @since 2021-10-24 commit cce8d262b0be3f70818c188725cd7e7fc4fdbb9a
	 */
	public void banUser() throws NamelessException {
		this.requests.post("users/" + this.getId() + "ban", null);
	}

}