package com.namelessmc.java_api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.Notification.NotificationType;
import com.namelessmc.java_api.RequestHandler.Action;

public final class NamelessUser {

	private final NamelessAPI api;
	private final RequestHandler requests;
	
	private int id;
	private String username;
	private String displayName;
	private Optional<UUID> uuid;
	private Date registeredDate;
	private boolean exists;
	private boolean banned;
	
	private VerificationInfo verification;
	
	private Group primaryGroup;
	private List<Group> secondaryGroups;
	
	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param uuid
	 * @param baseUrl Base API URL: <i>http(s)://yoursite.com/api/v2/API_KEY<i>
	 * @throws NamelessException
	 */
	NamelessUser(final NamelessAPI api, final UUID uuid) throws NamelessException {
		this.api = api;
		this.requests = api.getRequestHandler();
		
		this.init(this.requests.get(Action.USER_INFO, "uuid", uuid));
	}
	
	NamelessUser(final NamelessAPI api, final String username) throws NamelessException {
		this.api = api;
		this.requests = api.getRequestHandler();
		
		this.init(this.requests.get(Action.USER_INFO, "username", username));
	}
	
	NamelessUser(final NamelessAPI api, final int id) throws NamelessException {
		this.api = api;
		this.requests = api.getRequestHandler();
	
		this.init(this.requests.get(Action.USER_INFO, "id", id));
	}
	
	private void init(final JsonObject response) throws NamelessException {
		this.exists = response.get("exists").getAsBoolean();
		
		if (!this.exists) {
			return;
		}

		// Convert UNIX timestamp to date
		final Date registered = new Date(Long.parseLong(response.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		this.id = response.get("id").getAsInt();
		this.username = response.get("username").getAsString();
		this.displayName = response.get("displayname").getAsString();
		if (response.has("uuid")) {
			this.uuid = Optional.of(NamelessAPI.websiteUuidToJavaUuid(response.get("uuid").getAsString()));
		} else {
			this.uuid = Optional.empty();
		}
		this.registeredDate = registered;
		this.banned = response.get("banned").getAsBoolean();
		
		final boolean verified = response.get("verified").getAsBoolean();
		final JsonObject verification = response.getAsJsonObject("verification");
		this.verification = new VerificationInfo(verified, verification);
		
		final JsonObject groups = response.getAsJsonObject("groups");
		final JsonObject primary = groups.getAsJsonObject("primary");
		this.primaryGroup = new Group(primary.get("id").getAsInt(), primary.get("name").getAsString(), true);
		final List<Group> secondaryGroups = new ArrayList<>();
		groups.getAsJsonArray("secondary").forEach(e -> {
			final JsonObject group = e.getAsJsonObject();
			secondaryGroups.add(new Group(group.get("id").getAsInt(), group.get("name").getAsString(), false));
		});
		this.secondaryGroups = Collections.unmodifiableList(secondaryGroups);
		
	}

	/**
	 * @return The Minecraft username associated with the provided UUID. This is not always the name displayed on the website.
	 * @see #getDisplayName()
	 */
	public String getUsername() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This user does not exist.");
		}
		
		return this.username;
	}

	/**
	 * @return The name this player uses on the website. This is not always the same as their Minecraft username.
	 * @see #getUsername()
	 */
	public String getDisplayName() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This user does not exist.");
		}
		
		return this.displayName;
	}
	
	/**
	 * @return Minecraft UUID of this player. Empty if Minecraft integration is disabled.
	 * @see #getUsername()
	 */
	public Optional<UUID> getUniqueId() {
		return this.uuid;
	}

	/**
	 * @return The date the user registered on the website.
	 */
	public Date getRegisteredDate() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.registeredDate;
	}

	/**
	 * @return Whether the requested user exists
	 * @see #getUniqueId()
	 */
	public boolean exists() {
		// TODO Throw an exception in constructor instead when user does not exist
		return this.exists;
	}

	/**
	 * @return Whether this account is banned from the website.
	 */
	public boolean isBanned() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.banned;
	}
	
	public VerificationInfo getVerificationInfo() {
		return this.verification;
	}
	
	public Group getPrimaryGroup() {
		return this.primaryGroup;
	}
	
	public List<Group> getSecondaryGroups() {
		return this.secondaryGroups;
	}
	
	public List<Group> getGroups(){
		final List<Group> list = new ArrayList<>(this.secondaryGroups.size() + 1);
		list.add(this.primaryGroup);
		list.addAll(this.secondaryGroups);
		return Collections.unmodifiableList(list);
	}
	
	public List<Notification> getNotifications() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_NOTIFICATIONS, "id", this.id);
		
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
	 * Registers a new account. The user will be sent an email to set a password.
	 * @param username Username
	 * @param email Email address
	 * @return Email verification disabled: A link which the user needs to click to complete registration
	 * <br>Email verification enabled: An empty string (the user needs to check their email to complete registration)
	 * @throws NamelessException
	 */
	public Optional<String> register(final String username, final String email, final Optional<UUID> uuid) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		post.addProperty("email", email);
		if (uuid.isPresent()) {
			post.addProperty("uuid", uuid.get().toString());
		}
		
		final JsonObject response = this.requests.post(Action.REGISTER, post.toString());
		
		if (response.has("link")) {
			return Optional.of(response.get("link").getAsString());
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Reports a player
	 * @param username Username of the player or user to report
	 * @param reason Reason why this player has been reported
	 * @throws NamelessException
	 */
	public void createReport(final String username, final String reason) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("reporter", this.id);
		post.addProperty("reported", username);
		post.addProperty("content", reason);
		this.requests.post(Action.CREATE_REPORT, post.toString());
	}
	
	/**
	 * @param code
	 * @return True if the user could be validated successfully, false if the provided code is wrong
	 * @throws NamelessException
	 * @throws
	 */
	public boolean verifyMinecraft(final String code) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("id", this.id);
		post.addProperty("code", code);
		try {
			this.requests.post(Action.VERIFY_MINECRAFT, post.toString());
			return true;
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_VALIDATE_CODE) {
				return false;
			} else {
				throw e;
			}
		}
	}
	
	public void verifyDiscord(final String verificationToken) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void setDiscordIds(final long[] discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void addDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void removeDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public long[] getDiscordIds() {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

}