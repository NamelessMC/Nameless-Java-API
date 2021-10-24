package com.namelessmc.java_api;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.RequestHandler.Action;
import com.namelessmc.java_api.exception.CannotSendEmailException;
import com.namelessmc.java_api.exception.InvalidUsernameException;
import com.namelessmc.java_api.exception.UuidAlreadyExistsException;
import org.jetbrains.annotations.Nullable;

public final class NamelessAPI {

	@NotNull
	private final RequestHandler requests;

	NamelessAPI(@NotNull final RequestHandler requests) {
		this.requests = Objects.requireNonNull(requests, "Request handler is null");
	}

	@NotNull
	RequestHandler getRequestHandler() {
		return this.requests;
	}

	@NotNull
	public URL getApiUrl() {
		return this.getRequestHandler().getApiUrl();
	}

	@NotNull
	public String getApiKey() {
		return getApiKey(this.getApiUrl().toString());
	}

	@NotNull
	static String getApiKey(@NotNull final String url) {
		if (url.endsWith("/")) {
			return getApiKey(StringUtils.removeEnd(url, "/"));
		}

		return StringUtils.substringAfterLast(url, "/");
	}

	/**
	 * Get announcements visible to guests. Use {@link #getAnnouncements(NamelessUser)} for non-guest announcements.
	 * @return list of current announcements
	 * @throws NamelessException if there is an error in the request
	 */
	@NotNull
	public List<@NotNull Announcement> getAnnouncements() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_ANNOUNCEMENTS);

		return getAnnouncements(response);
	}

	/**
	 * Get all announcements visible for the player with the specified uuid
	 *
	 * @param user player to get visibile announcements for
	 * @return list of current announcements visible to the player
	 * @throws NamelessException if there is an error in the request
	 */
	@NotNull
	public List<@NotNull Announcement> getAnnouncements(@NotNull final NamelessUser user) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_ANNOUNCEMENTS, "id", user.getId());

		return getAnnouncements(response);
	}

	@NotNull
	private static Set<@NotNull String> toStringSet(@NotNull final JsonArray jsonArray) {
		return StreamSupport.stream(jsonArray.spliterator(), false).map(JsonElement::getAsString).collect(Collectors.toSet());
	}

	@NotNull
	private List<@NotNull Announcement> getAnnouncements(@NotNull final JsonObject response) {
		return jsonArrayToList(response.get("announcements").getAsJsonArray(), element -> {
			final JsonObject announcementJson = element.getAsJsonObject();
			final String content = announcementJson.get("content").getAsString();
			final Set<String> displayPages = toStringSet(announcementJson.get("display").getAsJsonArray());
			final Set<String> displayRanks = toStringSet(announcementJson.get("permissions").getAsJsonArray());
			return new Announcement(content, displayPages, displayRanks);
		});
	}

	private <T> @NotNull List<T> jsonArrayToList(@NotNull final JsonArray array, @NotNull final Function<JsonElement, T> elementSupplier) {
		return StreamSupport.stream(array.spliterator(), false).map(elementSupplier).collect(Collectors.toList());
	}

	public void submitServerInfo(@NotNull final JsonObject jsonData) throws NamelessException {
		this.requests.post(Action.SERVER_INFO, jsonData);
	}

	public Website getWebsite() throws NamelessException {
		final JsonObject json = this.requests.get(Action.INFO);
		return new Website(json);
	}

	public @NotNull List<NamelessUser> getRegisteredUsers(final @NotNull UserFilter<?>@Nullable... filters) throws NamelessException {
		final List<Object> parameters = new ArrayList<>();
		if (filters != null) {
			for (final UserFilter<?> filter : filters) {
				parameters.add(filter.getName());
				parameters.add(filter.getValue().toString());
			}
		}
		final JsonObject response = this.requests.get(Action.LIST_USERS, parameters.toArray());
		final JsonArray array = response.getAsJsonArray("users");
		final List<NamelessUser> users = new ArrayList<>(array.size());
		for (final JsonElement e : array) {
			final JsonObject o = e.getAsJsonObject();
			final int id = o.get("id").getAsInt();
			final String username = o.get("username").getAsString();
			final UUID uuid;
			if (o.has("uuid")) {
				final String uuidString = o.get("uuid").getAsString();
				if (uuidString == null || uuidString.equals("none") || uuidString.equals("")) {
					uuid = null;
				} else {
					uuid = NamelessAPI.websiteUuidToJavaUuid(uuidString);
				}
			} else {
				uuid = null;
			}
			users.add(new NamelessUser(this, id, username, true, uuid, false, -1L));
		}

		return Collections.unmodifiableList(users);
	}

	public @NotNull Optional<NamelessUser> getUser(final int id) throws NamelessException {
		final NamelessUser user = getUserLazy(id);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public @NotNull Optional<NamelessUser> getUser(@NotNull final String username) throws NamelessException {
		final NamelessUser user = getUserLazy(username);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public @NotNull Optional<NamelessUser> getUser(@NotNull final UUID uuid) throws NamelessException {
		final NamelessUser user = getUserLazy(uuid);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public @NotNull Optional<NamelessUser> getUserByDiscordId(final long discordId) throws NamelessException {
		final NamelessUser user = getUserLazyDiscord(discordId);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param id NamelessMC user id
	 * @return Nameless user object, never null
	 */
	public @NotNull NamelessUser getUserLazy(final int id) {
		return new NamelessUser(this, id, null, false, null, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param username NamelessMC user
	 * @return Nameless user object, never null
	 */
	public @NotNull NamelessUser getUserLazy(final @NotNull String username) {
		return new NamelessUser(this, -1, username, false, null, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param uuid Minecraft UUID
	 * @return Nameless user object, never null
	 */
	public @NotNull NamelessUser getUserLazy(@NotNull final UUID uuid) {
		return new NamelessUser(this, -1, null, true, uuid, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param username The user's username
	 * @param uuid The user's Mojang UUID
	 * @return Nameless user object, never null
	 */
	public NamelessUser getUserLazy(@NotNull final String username, @NotNull final UUID uuid) {
		return new NamelessUser(this, -1, username, true, uuid, false,-1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param id NamelessMC user id
	 * @return Nameless user object, never null
	 */
	public NamelessUser getUserLazy(final int id, final @NotNull String username, final @NotNull UUID uuid) {
		return new NamelessUser(this, id, username, true, uuid, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param discordId Discord user id
	 * @return Nameless user object, never null
	 */
	public NamelessUser getUserLazyDiscord(final long discordId) {
		Validate.isTrue(discordId > 0, "Discord id must be a positive long");
		return new NamelessUser(this, -1, null, false, null, true, discordId);
	}

	/**
	 * Get NamelessMC group by ID
	 * @param id Group id
	 * @return Optional with a group if the group exists, empty optional if it doesn't
	 */
	@NotNull
	public Optional<@NotNull Group> getGroup(final int id) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO, "id", id);
		final JsonArray jsonArray = response.getAsJsonArray("groups");
		if (jsonArray.size() != 1) {
			return Optional.empty();
		} else {
			return Optional.of(new Group(jsonArray.get(0).getAsJsonObject()));
		}
	}

	/**
	 * Get NamelessMC groups by name
	 * @param name NamelessMC groups name
	 * @return List of groups with this name, empty if there are no groups with this name.
	 */
	@NotNull
	public List<@NotNull Group> getGroup(@NotNull final String name) throws NamelessException {
		Objects.requireNonNull(name, "Group name is null");
		final JsonObject response = this.requests.get(Action.GROUP_INFO, "name", name);
		return groupListFromJsonArray(response.getAsJsonArray("groups"));
	}

	/**
	 * Get a list of all groups on the website
	 * @return list of groups
	 */
	public @NotNull List<Group> getAllGroups() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO);
		return groupListFromJsonArray(response.getAsJsonArray("groups"));

	}

	public int[] getAllGroupIds() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO);
		return StreamSupport.stream(response.getAsJsonArray("groups").spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.mapToInt(o -> o.get("id").getAsInt())
				.toArray();
	}

	private @NotNull List<Group> groupListFromJsonArray(@NotNull final JsonArray array) {
		return StreamSupport.stream(array.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.map(Group::new)
				.collect(Collectors.toList());
	}

	/**
	 * Registers a new account. The user will be sent an email to set a password.
	 *
	 * @param username Username (this should match the user's in-game username when specifying a uuid)
	 * @param email Email address
	 * @param uuid Mojang UUID, if you wish to use the Minecraft integration. Nullable.
	 * @return Email verification disabled: A link which the user needs to click to complete registration
	 * <br>Email verification enabled: An empty string (the user needs to check their email to complete registration)
	 * @see #registerUser(String, String)
	 */
	public @NotNull Optional<String> registerUser(@NotNull final String username,
												  @NotNull final String email,
												  @Nullable final UUID uuid)
			throws NamelessException, InvalidUsernameException, CannotSendEmailException, UuidAlreadyExistsException {
		Objects.requireNonNull(username, "Username is null");
		Objects.requireNonNull(email, "Email address is null");

		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		post.addProperty("email", email);
		if (uuid != null) {
			post.addProperty("uuid", uuid.toString());
		}

		try {
			final JsonObject response = this.requests.post(Action.REGISTER, post);

			if (response.has("link")) {
				return Optional.of(response.get("link").getAsString());
			} else {
				return Optional.empty();
			}
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_USERNAME) {
				throw new InvalidUsernameException();
			} else if (e.getError() == ApiError.UNABLE_TO_SEND_REGISTRATION_EMAIL) {
				throw new CannotSendEmailException();
			} else if (e.getError() == ApiError.UUID_ALREADY_EXISTS) {
				throw new UuidAlreadyExistsException();
			} else {
				throw e;
			}
		}
	}

	/**
	 * Convenience method for {@link #registerUser(String, String, UUID)} with null UUID.
	 * @param username New username for this user
	 * @param email New email address for this user
	 * @return Verification URL if email verification is disabled.
	 */
	public @NotNull Optional<String> registerUser(@NotNull final String username,
										 @NotNull final String email)
			throws NamelessException, InvalidUsernameException, CannotSendEmailException {
		try {
			return registerUser(username, email, null);
		} catch (final UuidAlreadyExistsException e) {
			throw new IllegalStateException("Website said duplicate uuid but we haven't specified a uuid?", e);
		}
	}

	public void verifyDiscord(@NotNull final String verificationToken,
							  final long discordUserId,
							  @NotNull final String discordUsername) throws NamelessException {
		Objects.requireNonNull(verificationToken, "Verification token is null");
		Objects.requireNonNull(discordUsername, "Discord username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("token", verificationToken);
		json.addProperty("discord_id", discordUserId + ""); // website needs it as a string
		json.addProperty("discord_username", discordUsername);
		this.requests.post(Action.VERIFY_DISCORD, json);
	}

	public void setDiscordBotUrl(@NotNull final URL url) throws NamelessException {
		Objects.requireNonNull(url, "Bot url is null");

		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void setDiscordGuildId(final long guildId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("guild_id", guildId + "");
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void setDiscordBotUser(@NotNull final String username, final long userId) throws NamelessException {
		Objects.requireNonNull(username, "Bot username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void setDiscordBotSettings(@NotNull final URL url, final long guildId, @NotNull final String username, final long userId) throws NamelessException {
		Objects.requireNonNull(url, "Bot url is null");
		Objects.requireNonNull(username, "Bot username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		json.addProperty("guild_id", guildId + "");
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void submitDiscordRoleList(@NotNull final Map<Long, String> discordRoles) throws NamelessException {
		final JsonArray roles = new JsonArray();
		discordRoles.forEach((id, name) -> {
			final JsonObject role = new JsonObject();
			role.addProperty("id", id);
			role.addProperty("name", name);
			roles.add(role);
		});
		final JsonObject json = new JsonObject();
		json.add("roles", roles);
		this.requests.post(Action.SUBMIT_DISCORD_ROLE_LIST, json);
	}

	public void updateDiscordUsername(final long discordUserId, @NotNull final String discordUsername) throws NamelessException {
		Objects.requireNonNull(discordUsername, "Discord username is null");

		final JsonObject user = new JsonObject();
		user.addProperty("id", discordUserId);
		user.addProperty("name", discordUsername);
		final JsonArray users = new JsonArray();
		users.add(user);
		final JsonObject json = new JsonObject();
		json.add("users", users);
		this.requests.post(Action.UPDATE_DISCORD_USERNAMES, json);
	}

	public void updateDiscordUsernames(final long@NotNull [] discordUserIds, @NotNull final String[] discordUsernames) throws NamelessException {
		Objects.requireNonNull(discordUserIds, "User ids array is null");
		Objects.requireNonNull(discordUsernames, "Usernames array is null");

		if (discordUserIds.length != discordUsernames.length) {
			throw new IllegalArgumentException("discord user ids and discord usernames must be of same length");
		}

		if (discordUserIds.length == 0) {
			return;
		}

		final JsonArray users = new JsonArray();

		for (int i = 0; i < discordUserIds.length; i++) {
			final JsonObject user = new JsonObject();
			user.addProperty("id", discordUserIds[i]);
			user.addProperty("name", discordUsernames[i]);
			users.add(user);
		}

		final JsonObject json = new JsonObject();
		json.add("users", users);
		this.requests.post(Action.UPDATE_DISCORD_USERNAMES, json);
	}

	@NotNull
	static UUID websiteUuidToJavaUuid(@NotNull final String uuid) {
		Objects.requireNonNull(uuid, "UUID string is null");
		// Website sends UUIDs without dashses, so we can't use UUID#fromString
		// https://stackoverflow.com/a/30760478
		try {
			final BigInteger a = new BigInteger(uuid.substring(0, 16), 16);
			final BigInteger b = new BigInteger(uuid.substring(16, 32), 16);
			return new UUID(a.longValue(), b.longValue());
		} catch (final IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Invalid uuid: '" + uuid + "'", e);
		}
	}

	@NotNull
	public static NamelessApiBuilder builder() {
		return new NamelessApiBuilder();
	}

}
