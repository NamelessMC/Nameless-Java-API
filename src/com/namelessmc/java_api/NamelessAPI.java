package com.namelessmc.java_api;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.*;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.modules.websend.WebsendAPI;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class NamelessAPI {

	static final Gson GSON = new Gson();

	private final @NonNull RequestHandler requests;

	// Not actually used by the Nameless Java API, but could be useful to applications using it.
	private final @NonNull URL apiUrl;
	private final @NonNull String apiKey;

	NamelessAPI(final @NonNull RequestHandler requests,
				final @NonNull URL apiUrl,
				final @NonNull String apiKey) {
		this.requests = Objects.requireNonNull(requests, "Request handler is null");
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
	}


	@NonNull RequestHandler getRequestHandler() {
		return this.requests;
	}


	public @NonNull URL getApiUrl() {
		return this.apiUrl;
	}


	public @NonNull String getApiKey() {
		return this.apiKey;
	}

	/**
	 * Get announcements visible to guests. Use {@link NamelessUser#getAnnouncements()} for non-guest announcements.
	 * @return List of announcements
	 */

	public @NonNull List<@NonNull Announcement> getAnnouncements() throws NamelessException {
		final JsonObject response = this.requests.get("announcements");
		return getAnnouncements(response);
	}

	/**
	 * Get announcements visible to a {@link NamelessUser}
	 * @param user User to get visible announcements for
	 * @return List of announcements visible to the user
	 * @deprecated Use {@link NamelessUser#getAnnouncements()}
	 */
	@Deprecated
	public @NonNull List<@NonNull Announcement> getAnnouncements(final @NonNull NamelessUser user) throws NamelessException {
		final JsonObject response = this.requests.get("users/" + user.getUserTransformer() + "/announcements");

		return getAnnouncements(response);
	}

	/**
	 * Convert announcement json to objects
	 * @param response Announcements json API response
	 * @return List of {@link Announcement} objects
	 */
	static @NonNull List<@NonNull Announcement> getAnnouncements(final @NonNull JsonObject response) {
		return StreamSupport.stream(response.getAsJsonArray("announcements").spliterator(), false)
					.map(JsonElement::getAsJsonObject)
					.map(Announcement::new)
					.collect(Collectors.toList());
	}

	/**
	 * Send Minecraft server information to the website. Currently, the exact JSON contents are undocumented.
	 * @param jsonData Json data to submit
	 */
	public void submitServerInfo(final @NonNull JsonObject jsonData) throws NamelessException {
		this.requests.post("minecraft/server-info", jsonData);
	}

	/**
	 * Get website information
	 * @return {@link Website} object containing website information
	 */
	public Website getWebsite() throws NamelessException {
		final JsonObject json = this.requests.get("info");
		return new Website(json);
	}

	public FilteredUserListBuilder getRegisteredUsers() {
		return new FilteredUserListBuilder(this);
	}

	public @NonNull Optional<NamelessUser> getUser(final int id) throws NamelessException {
		final NamelessUser user = getUserLazy(id);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public @NonNull Optional<NamelessUser> getUser(final @NonNull String username) throws NamelessException {
		final NamelessUser user = getUserLazy(username);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public @NonNull Optional<NamelessUser> getUser(final @NonNull UUID uuid) throws NamelessException {
		final NamelessUser user = getUserLazy(uuid);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public @NonNull Optional<NamelessUser> getUserByDiscordId(final long discordId) throws NamelessException {
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
	public @NonNull NamelessUser getUserLazy(final int id) {
		return new NamelessUser(this, id, null, false, null, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param username NamelessMC user
	 * @return Nameless user object, never null
	 */
	public @NonNull NamelessUser getUserLazy(final @NonNull String username) {
		return new NamelessUser(this, -1, username, false, null, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param uuid Minecraft UUID
	 * @return Nameless user object, never null
	 */
	public @NonNull NamelessUser getUserLazy(final @NonNull UUID uuid) {
		return new NamelessUser(this, -1, null, true, uuid, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param username The user's username
	 * @param uuid The user's Mojang UUID
	 * @return Nameless user object, never null
	 */
	public NamelessUser getUserLazy(final @NonNull String username, final @NonNull UUID uuid) {
		return new NamelessUser(this, -1, username, true, uuid, false,-1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param id NamelessMC user id
	 * @return Nameless user object, never null
	 */
	public NamelessUser getUserLazy(final int id, final @NonNull String username, final @NonNull UUID uuid) {
		return new NamelessUser(this, id, username, true, uuid, false, -1L);
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param discordId Discord user id
	 * @return Nameless user object, never null
	 */
	public NamelessUser getUserLazyDiscord(final long discordId) {
		Preconditions.checkArgument(discordId > 0, "Discord id must be a positive long");
		return new NamelessUser(this, -1, null, false, null, true, discordId);
	}

	/**
	 * Get NamelessMC group by ID
	 * @param id Group id
	 * @return Optional with a group if the group exists, empty optional if it doesn't
	 */
	public @NonNull Optional<@NonNull Group> getGroup(final int id) throws NamelessException {
		final JsonObject response = this.requests.get("groups", "id", id);
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
	public @NonNull List<@NonNull Group> getGroup(final @NonNull String name) throws NamelessException {
		Objects.requireNonNull(name, "Group name is null");
		final JsonObject response = this.requests.get("groups", "name", name);
		return groupListFromJsonArray(response.getAsJsonArray("groups"));
	}

	/**
	 * Get a list of all groups on the website
	 * @return list of groups
	 */
	public @NonNull List<Group> getAllGroups() throws NamelessException {
		final JsonObject response = this.requests.get("groups");
		return groupListFromJsonArray(response.getAsJsonArray("groups"));

	}

	public int @NonNull[] getAllGroupIds() throws NamelessException {
		final JsonObject response = this.requests.get("groups");
		return StreamSupport.stream(response.getAsJsonArray("groups").spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.mapToInt(o -> o.get("id").getAsInt())
				.toArray();
	}

	private @NonNull List<Group> groupListFromJsonArray(final @NonNull JsonArray array) {
		return StreamSupport.stream(array.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.map(Group::new)
				.collect(Collectors.toList());
	}

	/**
	 * Registers a new account. The user will be emailed to set a password.
	 *
	 * @param username Username (this should match the user's in-game username when specifying a UUID)
	 * @param email Email address
	 * @param integrationData Integration data objects. By supplying account information here, the user will
	 *                        an account connection will automatically be created without the user needing to
	 *                        verify.
	 * @return Email verification disabled: A link which the user needs to click to complete registration
	 * <br>Email verification enabled: An empty string (the user needs to check their email to complete registration)
	 */
	public @NonNull Optional<String> registerUser(final @NonNull String username,
												  final @NonNull String email,
												  final @NonNull IntegrationData@Nullable ... integrationData)
			throws NamelessException, InvalidUsernameException, UsernameAlreadyExistsException,
					CannotSendEmailException, IntegrationUsernameAlreadyExistsException,
					IntegrationIdAlreadyExistsException, InvalidEmailAddressException, EmailAlreadyUsedException {

		Objects.requireNonNull(username, "Username is null");
		Objects.requireNonNull(email, "Email address is null");

		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		post.addProperty("email", email);
		if (integrationData != null && integrationData.length > 0) {
			JsonObject integrationsJson = new JsonObject();
			for (IntegrationData integration : integrationData) {
				JsonObject integrationJson = new JsonObject();
				integrationJson.addProperty("identifier", integration.getIdentifier());
				integrationJson.addProperty("username", integration.getUsername());
				integrationsJson.add(integration.getIntegrationType().toString(), integrationJson);
			}
			post.add("integrations", integrationsJson);
		}

		try {
			final JsonObject response = this.requests.post("users/register", post);

			if (response.has("link")) {
				return Optional.of(response.get("link").getAsString());
			} else {
				return Optional.empty();
			}
		} catch (final ApiError e) {
			switch (e.getError()) {
				case ApiError.INVALID_USERNAME: throw new InvalidUsernameException();
				case ApiError.USERNAME_ALREADY_EXISTS: throw new UsernameAlreadyExistsException();
				case ApiError.UNABLE_TO_SEND_REGISTRATION_EMAIL: throw new CannotSendEmailException();
				case ApiError.INTEGRATION_USERNAME_ALREADY_EXISTS: throw new IntegrationUsernameAlreadyExistsException();
				case ApiError.INTEGRATION_ID_ALREADY_EXISTS: throw new IntegrationIdAlreadyExistsException();
				case ApiError.INVALID_EMAIL_ADDRESS: throw new InvalidEmailAddressException();
				case ApiError.EMAIL_ALREADY_EXISTS: throw new EmailAlreadyUsedException();
				default: throw e;
			}
		}
	}

	/**
	 * Set Discord bot URL (Nameless-Link internal webserver)
	 * @param url Discord bot URL
	 */
	public void setDiscordBotUrl(final @NonNull URL url) throws NamelessException {
		Objects.requireNonNull(url, "Bot url is null");

		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Set Discord guild (server) id
	 * @param guildId Discord guild (server) id
	 */
	public void setDiscordGuildId(final long guildId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("guild_id", guildId + "");
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Set discord bot username and user id
	 * @param username Bot username#tag
	 * @param userId Bot user id
	 * @see #setDiscordBotSettings(URL, long, String, long)
	 */
	public void setDiscordBotUser(final @NonNull String username, final long userId) throws NamelessException {
		Objects.requireNonNull(username, "Bot username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Update all Discord bot settings.
	 * @param url Discord bot URL
	 * @param guildId Discord guild (server) id
	 * @param username Discord bot username#tag
	 * @param userId Discord bot user id
	 * @see #setDiscordBotUrl(URL)
	 * @see #setDiscordGuildId(long)
	 * @see #setDiscordBotUser(String, long)
	 */
	public void setDiscordBotSettings(final @NonNull URL url,
									  final long guildId,
									  final @NonNull String username,
									  final long userId) throws NamelessException {
		Objects.requireNonNull(url, "Bot url is null");
		Objects.requireNonNull(username, "Bot username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		json.addProperty("guild_id", guildId + "");
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Send list of Discord roles to the website for populating the dropdown in StaffCP > API > Group sync
	 * @param discordRoles Map of Discord roles, key is role id, value is role name
	 */
	public void submitDiscordRoleList(final @NonNull Map<Long, String> discordRoles) throws NamelessException {
		final JsonArray roles = new JsonArray();
		discordRoles.forEach((id, name) -> {
			final JsonObject role = new JsonObject();
			role.addProperty("id", id);
			role.addProperty("name", name);
			roles.add(role);
		});
		final JsonObject json = new JsonObject();
		json.add("roles", roles);
		this.requests.post("discord/submit-role-list", json);
	}

	/**
	 * Update Discord username for a NamelessMC user associated with the provided Discord user id
	 * @param discordUserId Discord user id
	 * @param discordUsername New Discord [username#tag]s
	 * @see #updateDiscordUsernames(long[], String[])
	 */
	public void updateDiscordUsername(final long discordUserId,
									  final @NonNull String discordUsername)
			throws NamelessException {
		Objects.requireNonNull(discordUsername, "Discord username is null");

		final JsonObject user = new JsonObject();
		user.addProperty("id", discordUserId);
		user.addProperty("name", discordUsername);
		final JsonArray users = new JsonArray();
		users.add(user);
		final JsonObject json = new JsonObject();
		json.add("users", users);
		this.requests.post("discord/update-usernames", json);
	}

	/**
	 * Update Discord usernames in bulk
	 * @param discordUserIds Discord user ids
	 * @param discordUsernames New Discord [username#tag]s
	 * @see #updateDiscordUsername(long, String)
	 */
	public void updateDiscordUsernames(final long@NonNull[] discordUserIds,
									   final  @NonNull String@NonNull[] discordUsernames)
			throws NamelessException {
		Objects.requireNonNull(discordUserIds, "User ids array is null");
		Objects.requireNonNull(discordUsernames, "Usernames array is null");
		Preconditions.checkArgument(discordUserIds.length == discordUsernames.length,
				"discord user ids and discord usernames must be of same length");

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
		this.requests.post("discord/update-usernames", json);
	}

	public void verifyIntegration(final @NonNull IntegrationData integrationData,
								   final @NonNull String verificationCode)
			throws NamelessException, InvalidValidateCodeException {
		JsonObject data = new JsonObject();
		data.addProperty("integration", integrationData.getIntegrationType());
		data.addProperty("identifier", integrationData.getIdentifier());
		data.addProperty("username", integrationData.getUsername());
		data.addProperty("code", Objects.requireNonNull(verificationCode, "Verification code is null"));
		try {
			this.requests.post("integration/verify", data);
		} catch (ApiError e) {
			switch (e.getError()) {
				case ApiError.INVALID_VALIDATE_CODE:
					throw new InvalidValidateCodeException();
				default:
					throw e;
			}
		}
	}

	public @NonNull WebsendAPI websend() {
		return new WebsendAPI(this.requests);
	}

	/**
	 * Adds back dashes to a UUID string and converts it to a Java UUID object
	 * @param uuid UUID without dashes
	 * @return UUID with dashes
	 */
	public static @NonNull UUID websiteUuidToJavaUuid(final @NonNull String uuid) {
		Objects.requireNonNull(uuid, "UUID string is null");
		// Website sends UUIDs without dashes, so we can't use UUID#fromString
		// https://stackoverflow.com/a/30760478
		try {
			final BigInteger a = new BigInteger(uuid.substring(0, 16), 16);
			final BigInteger b = new BigInteger(uuid.substring(16, 32), 16);
			return new UUID(a.longValue(), b.longValue());
		} catch (final IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Invalid uuid: '" + uuid + "'", e);
		}
	}

	public static @NonNull NamelessApiBuilder builder(final @NonNull URL apiUrl,
											 final @NonNull String apiKey) {
		return new NamelessApiBuilder(apiUrl, apiKey);
	}

}
