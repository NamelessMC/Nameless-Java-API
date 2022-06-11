package com.namelessmc.java_api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.integrations.IntegrationData;
import com.namelessmc.java_api.modules.discord.DiscordAPI;
import com.namelessmc.java_api.modules.store.StoreAPI;
import com.namelessmc.java_api.modules.websend.WebsendAPI;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class NamelessAPI {

	private final @NonNull RequestHandler requests;

	// Not actually used by the Nameless Java API, but could be useful to applications using it.
	private final @NonNull URL apiUrl;
	private final @NonNull String apiKey;

	private static final long CACHED_WEBSITE_INFO_VALIDITY = 60_000;
	private @Nullable Website cachedWebsiteInfo = null;
	private long cachedWebsiteInfoTime = 0;

	NamelessAPI(final @NonNull RequestHandler requests,
				final @NonNull URL apiUrl,
				final @NonNull String apiKey) {
		this.requests = Objects.requireNonNull(requests, "Request handler is null");
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
	}

	public @NonNull RequestHandler requests() {
		return this.requests;
	}

	public @NonNull URL apiUrl() {
		return this.apiUrl;
	}

	public @NonNull String apiKey() {
		return this.apiKey;
	}

	/**
	 * Get announcements visible to guests. Use {@link NamelessUser#announcements()} for non-guest announcements.
	 * @return List of announcements
	 */

	public @NonNull List<@NonNull Announcement> announcements() throws NamelessException {
		final JsonObject response = this.requests.get("announcements");
		return announcements(response);
	}

	/**
	 * Convert announcement json to objects
	 * @param response Announcements json API response
	 * @return List of {@link Announcement} objects
	 */
	static @NonNull List<@NonNull Announcement> announcements(final @NonNull JsonObject response) {
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
	public Website website() throws NamelessException {
		if (this.cachedWebsiteInfoTime + CACHED_WEBSITE_INFO_VALIDITY > System.currentTimeMillis() &&
				this.cachedWebsiteInfo != null) {
			return this.cachedWebsiteInfo;
		}

		final JsonObject json = this.requests.get("info");
		final Website website = new Website(json);
		this.cachedWebsiteInfo = website;
		this.cachedWebsiteInfoTime = System.currentTimeMillis();
		return website;
	}

	public FilteredUserListBuilder users() {
		return new FilteredUserListBuilder(this);
	}

	public @Nullable NamelessUser userAsNullable(NamelessUser user) throws NamelessException {
		try {
			user.userInfo();
			return user;
		} catch (ApiException e) {
			if (e.apiError() == ApiError.NAMELESS_CANNOT_FIND_USER) {
				return null;
			}
			throw e;
		}
	}

	public @Nullable NamelessUser user(final int id) throws NamelessException {
		return userAsNullable(userLazy(id));
	}

	public @Nullable NamelessUser userByUsername(final @NonNull String username) throws NamelessException {
		return userAsNullable(userByUsernameLazy(username));
	}

	public @Nullable NamelessUser userByMinecraftUuid(final @NonNull UUID uuid) throws NamelessException {
		return userAsNullable(userByMinecraftUuidLazy(uuid));
	}

	public @Nullable NamelessUser userByMinecraftUsername(final @NonNull String username) throws NamelessException {
		return userAsNullable(userByMinecraftUsernameLazy(username));
	}

	public @Nullable NamelessUser userByDiscordId(final long id) throws NamelessException {
		return userAsNullable(userByDiscordIdLazy(id));
	}

	public @Nullable NamelessUser userByDiscordUsername(final @NonNull String username) throws NamelessException {
		return userAsNullable(userByDiscordUsernameLazy(username));
	}

	/**
	 * Construct a NamelessUser object without making API requests (so without checking if the user exists)
	 * @param id NamelessMC user id
	 * @return Nameless user object, never null
	 */
	public @NonNull NamelessUser userLazy(final int id) {
		return new NamelessUser(this, id);
	}

	public @NonNull NamelessUser userLazy(final @NonNull String userTransformer) {
		return new NamelessUser(this, userTransformer);
	}

	public @NonNull NamelessUser userByUsernameLazy(final @NonNull String username) {
		return userLazy("username:" + username);
	}

	public @NonNull NamelessUser userByMinecraftUuidLazy(final @NonNull UUID uuid) {
		return userLazy("integration_id:minecraft:" + javaUuidToWebsiteUuid(uuid));
	}

	public @NonNull NamelessUser userByMinecraftUsernameLazy(final @NonNull String username) {
		return userLazy("integration_username:minecraft:" + username);
	}

	public @NonNull NamelessUser userByDiscordIdLazy(final long id) {
		return userLazy("integration_id:discord:" + id);
	}

	public @NonNull NamelessUser userByDiscordUsernameLazy(final @NonNull String username) {
		return userLazy("integration_username:discord:" + username);
	}

	/**
	 * Get NamelessMC group by ID
	 * @param id Group id
	 * @return Group or null if it doesn't exist
	 */
	public @Nullable Group group(final int id) throws NamelessException {
		final JsonObject response = this.requests.get("groups", "id", id);
		final JsonArray jsonArray = response.getAsJsonArray("groups");
		if (jsonArray.size() == 1) {
			return new Group(jsonArray.get(0).getAsJsonObject());
		} else if (jsonArray.isEmpty()) {
			return null;
		} else {
			throw new IllegalStateException("Website returned multiple groups for one id");
		}
	}

	/**
	 * Get NamelessMC groups by name
	 * @param name NamelessMC groups name
	 * @return List of groups with this name, empty if there are no groups with this name.
	 */
	public List<Group> group(final @NonNull String name) throws NamelessException {
		Objects.requireNonNull(name, "Group name is null");
		final JsonObject response = this.requests.get("groups", "name", name);
		return groupListFromJsonArray(response.getAsJsonArray("groups"));
	}

	/**
	 * Get a list of all groups on the website
	 * @return list of groups
	 */
	public List<Group> getAllGroups() throws NamelessException {
		final JsonObject response = this.requests.get("groups");
		return groupListFromJsonArray(response.getAsJsonArray("groups"));

	}

	public int[] getAllGroupIds() throws NamelessException {
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
	public Optional<String> registerUser(final @NonNull String username,
												  final @NonNull String email,
												  final @NonNull IntegrationData@Nullable ... integrationData)
			throws NamelessException {

		Objects.requireNonNull(username, "Username is null");
		Objects.requireNonNull(email, "Email address is null");

		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		post.addProperty("email", email);
		if (integrationData != null && integrationData.length > 0) {
			JsonObject integrationsJson = new JsonObject();
			for (IntegrationData integration : integrationData) {
				JsonObject integrationJson = new JsonObject();
				integrationJson.addProperty("identifier", integration.identifier());
				integrationJson.addProperty("username", integration.username());
				integrationsJson.add(integration.type().toString(), integrationJson);
			}
			post.add("integrations", integrationsJson);
		}

		final JsonObject response = this.requests.post("users/register", post);

		if (response.has("link")) {
			return Optional.of(response.get("link").getAsString());
		} else {
			return Optional.empty();
		}
	}

	public void verifyIntegration(final @NonNull IntegrationData integrationData,
								   final @NonNull String verificationCode) throws NamelessException {
		JsonObject data = new JsonObject();
		data.addProperty("integration", integrationData.type());
		data.addProperty("identifier", integrationData.identifier());
		data.addProperty("username", integrationData.username());
		data.addProperty("code", Objects.requireNonNull(verificationCode, "Verification code is null"));
		this.requests.post("integration/verify", data);
	}

	public DiscordAPI discord() {
		return new DiscordAPI(this);
	}

	public StoreAPI store() {
		return new StoreAPI(this);
	}

	public WebsendAPI websend() {
		return new WebsendAPI(this);
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

	public static @NonNull String javaUuidToWebsiteUuid(final @NonNull UUID uuid) {
		return uuid.toString().replace("-", "");
	}

	public static @NonNull NamelessApiBuilder builder(final @NonNull URL apiUrl,
											 final @NonNull String apiKey) {
		return new NamelessApiBuilder(apiUrl, apiKey);
	}

}
