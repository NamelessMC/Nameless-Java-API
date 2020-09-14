package com.namelessmc.NamelessAPI;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Notification.NotificationType;
import com.namelessmc.NamelessAPI.Request.Action;

public final class NamelessPlayer {

	private String userName;
	private String displayName;
	private final UUID uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;
	private String groupName;
	
	private final URL baseUrl;
	private final String userAgent;
	
	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param uuid
	 * @param baseUrl Base API URL: <i>http(s)://yoursite.com/api/v2/API_KEY<i>
	 * @throws NamelessException
	 */
	NamelessPlayer(final UUID uuid, final URL baseUrl, final String userAgent) throws NamelessException {
		this.uuid = uuid;
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		
		final Request request = new Request(baseUrl, userAgent, Action.USER_INFO, new ParameterBuilder().add("uuid", uuid).build());
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

		this.userName = response.get("username").getAsString();
		this.displayName = response.get("displayname").getAsString();
		//uuid = UUID.fromString(addDashesToUUID(response.get("uuid").getAsString()));
		this.groupName = response.get("group_name").getAsString();
		this.groupID = response.get("group_id").getAsInt();
		this.registeredDate = registered;
		this.validated = response.get("validated").getAsBoolean();
		//reputation = response.get("reputation").getAsInt();
		this.reputation = 0; // temp until reputation is added to API
		this.banned = response.get("banned").getAsBoolean();
	}

	/*public static String addDashesToUUID(String uuid) {
		// https://bukkit.org/threads/java-adding-dashes-back-to-minecrafts-uuids.272746/
		StringBuffer sb = new StringBuffer(uuid);
		sb.insert(8, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(13, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(18, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(23, "-");
		 
		return sb.toString();
	}*/
	
	/**
	 * @return The Minecraft username associated with the provided UUID. This is not always the name displayed on the website.
	 * @see #getDisplayName()
	 */
	public String getUsername() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.userName;
	}

	/**
	 * @return The name this player uses on the website. This is not always the same as their Minecraft username.
	 * @see #getUsername()
	 */
	public String getDisplayName() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.displayName;
	}
	
	/**
	 * @return Minecraft UUID of this player.
	 * @see #getUsername()
	 */
	public UUID getUniqueId() {
		return this.uuid;
	}

	/**
	 * @return A numerical group id.
	 */
	public int getGroupID() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.groupID;
	}

	/**
	 * @return The user's primary group name
	 */
	public String getGroupName() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}

		return this.groupName;
	}

	/**
	 * @return The user's site reputation.
	 */
	public int getReputation() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.reputation;
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
	 * @return Whether an account associated with the UUID exists.
	 * @see #getUniqueId()
	 */
	public boolean exists() {
		return this.exists;
	}

	/**
	 * @return Whether this account has been validated. An account is validated when a password is set.
	 */
	public boolean isValidated() {
		if (!this.exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return this.validated;
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
	
	/**
	 * @param code
	 * @return True if the user could be validated successfully, false if the provided code is wrong
	 * @throws NamelessException
	 * @throws
	 */
	public boolean validate(final String code) throws NamelessException {
		final String[] params = new ParameterBuilder()
				.add("uuid", this.uuid)
				.add("code", code).build();
		final Request request = new Request(this.baseUrl,  this.userAgent, Action.VALIDATE_USER, params);
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
	
	public void verifyDiscord(final String verificationToken) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void setDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void setDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void setDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

}