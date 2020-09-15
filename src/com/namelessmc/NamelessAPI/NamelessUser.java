package com.namelessmc.NamelessAPI;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Notification.NotificationType;
import com.namelessmc.NamelessAPI.Request.Action;

public final class NamelessUser {

	private String username;
	private String displayName;
	private Optional<UUID> uuid;
	private Date registeredDate;
	private boolean exists;
	private boolean banned;
	
	private VerificationInfo verification;
	
	private Group primaryGroup;
	private List<Group> secondaryGroups;
	
	private final URL baseUrl;
	private final String userAgent;
	
	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param uuid
	 * @param baseUrl Base API URL: <i>http(s)://yoursite.com/api/v2/API_KEY<i>
	 * @throws NamelessException
	 */
	NamelessUser(final UUID uuid, final URL baseUrl, final String userAgent) throws NamelessException {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		
		final Request request = new Request(baseUrl, userAgent, Action.USER_INFO, new ParameterBuilder().add("uuid", uuid).build());
		this.init(request);
	}
	
	NamelessUser(String username, URL baseUrl, String userAgent) throws NamelessException {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		
		final Request request = new Request(baseUrl, userAgent, Action.USER_INFO, new ParameterBuilder().add("username", username).build());
		this.init(request);
	}
	
	NamelessUser(int id, URL baseUrl, String userAgent) throws NamelessException {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		
		final Request request = new Request(baseUrl, userAgent, Action.USER_INFO, new ParameterBuilder().add("id", id).build());
		this.init(request);
	}
	
	private void init(final Request request) throws NamelessException {
		request.connect();
		
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
		
		final JsonObject response = request.getResponse();
		
		this.exists = response.get("exists").getAsBoolean();
		
		if (!this.exists) {
			return;
		}

		// Convert UNIX timestamp to date
		final Date registered = new Date(Long.parseLong(response.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		this.username = response.get("username").getAsString();
		this.displayName = response.get("displayname").getAsString();
		if (response.has("uuid")) {
			uuid = Optional.of(NamelessAPI.websiteUuidToJavaUuid(response.get("uuid").getAsString()));
		} else {
			uuid = Optional.empty();
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
		final Request request = new Request(this.baseUrl, this.userAgent, Action.GET_NOTIFICATIONS, new ParameterBuilder().add("uuid", this.uuid).build());
		request.connect();
		
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
		
		final List<Notification> notifications = new ArrayList<>();
		
		final JsonObject object = request.getResponse();
		object.getAsJsonArray("notifications").forEach((element) -> {
			final String message = element.getAsJsonObject().get("message").getAsString();
			final String url = element.getAsJsonObject().get("url").getAsString();
			final NotificationType type = NotificationType.fromString(element.getAsJsonObject().get("type").getAsString());
			notifications.add(new Notification(message, url, type));
		});
		
		return notifications;
	}
	
	/**
	 * Sets the players group
	 * @param groupId Numerical ID associated with a group
	 * @throws NamelessException
	 */
	public void setGroup(final int groupId) throws NamelessException {
		final String[] parameters = new ParameterBuilder().add("uuid", this.uuid).add("group_id", groupId).build();
		final Request request = new Request(this.baseUrl, this.userAgent,  Action.SET_GROUP, parameters);
		request.connect();
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
	}
	
	/**
	 * Registers a new account. The player will be sent an email to set a password.
	 * @param minecraftName In-game name for this player
	 * @param email Email address
	 * @return Email verification disabled: A link which the user needs to click to complete registration
	 * <br>Email verification enabled: An empty string (the user needs to check their email to complete registration)
	 * @throws NamelessException
	 */
	public String register(final String minecraftName, final String email) throws NamelessException {
		final String[] parameters = new ParameterBuilder().add("username", minecraftName).add("uuid", this.uuid).add("email", email).build();
		final Request request = new Request(this.baseUrl, this.userAgent,  Action.REGISTER, parameters);
		request.connect();
		
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
		
		final JsonObject response = request.getResponse();
		
		if (response.has("link")) {
			return response.get("link").getAsString();
		} else {
			return "";
		}
	}

	/**
	 * Reports a player
	 * @param reportedUuid UUID of the reported player
	 * @param reportedUsername In-game name of the reported player
	 * @param reason Reason why this player has been reported
	 * @throws NamelessException
	 */
	public void createReport(final UUID reportedUuid, final String reportedUsername, final String reason) throws NamelessException {
		final String[] parameters = new ParameterBuilder()
				.add("reporter_uuid", this.uuid)
				.add("reported_uuid", reportedUuid)
				.add("reported_username", reportedUsername)
				.add("content", reason)
				.build();
		final Request request = new Request(this.baseUrl, this.userAgent,  Action.CREATE_REPORT, parameters);
		request.connect();
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
	}
	
	/**
	 * @param code
	 * @return True if the user could be validated successfully, false if the provided code is wrong
	 * @throws NamelessException
	 * @throws
	 */
	public boolean verifyMinecraft(final String code) throws NamelessException {
		final String[] params = new ParameterBuilder()
				.add("uuid", this.uuid)
				.add("code", code).build();
		final Request request = new Request(this.baseUrl, this.userAgent, Action.VALIDATE_USER, params);
		request.connect();
		
		if (request.hasError()) {
			if (request.getError() == ApiError.INVALID_VALIDATE_CODE) {
				return false;
			} else {
				throw new ApiError(request.getError());
			}
		} else {
			return true;
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