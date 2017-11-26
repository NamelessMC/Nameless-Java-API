package com.namelessmc.NamelessAPI;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Request.Action;

public final class NamelessPlayer {

	private String userName;
	private String displayName;
	private UUID uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;
	
	private URL baseUrl;

	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param uuid
	 * @param baseUrl Base API URL: <i>http(s)://yoursite.com/api/v2/API_KEY<i>
	 * @throws NamelessException 
	 * @see #NamelessPlayer(String, URL)
	 */
	public NamelessPlayer(UUID uuid, URL baseUrl) throws NamelessException {	
		this.baseUrl = baseUrl;
		
		Request request = new Request(baseUrl, Action.USER_INFO, new ParameterBuilder().add("uuid", uuid).build());
		init(request.getResponse());
	}
	
	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param username
	 * @param baseUrl Base API URL: <i>http(s)://yoursite.com/api/v2/API_KEY<i>
	 * @throws NamelessException 
	 * @see #NamelessPlayer(UUID, URL)
	 */
	public NamelessPlayer(String username, URL baseUrl) throws NamelessException {	
		this.baseUrl = baseUrl;
		
		Request request = new Request(baseUrl, Action.USER_INFO, new ParameterBuilder().add("username", username).build());
		init(request.getResponse());
	}
	
	private void init(JsonObject response) {			
		exists = response.get("exists").getAsBoolean();
		
		if (!exists) {
			return;
		}

		// Convert UNIX timestamp to date
		Date registered = new Date(Long.parseLong(response.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		// Display get user.
		userName = response.get("username").getAsString();
		displayName = response.get("displayname").getAsString();
		uuid = UUID.fromString(addDashesToUUID(response.get("uuid").getAsString()));
		groupID = response.get("group_id").getAsInt();
		registeredDate = registered;
		validated = response.get("validated").getAsBoolean();
		reputation = response.get("reputation").getAsInt();
		banned = response.get("banned").getAsBoolean();
	}

	public static String addDashesToUUID(String uuid) {
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
	}
	
	/**
	 * @return The Minecraft username associated with the provided UUID. This is not always the name displayed on the website.
	 * @see #getDisplayName()
	 */
	public String getUsername() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return userName;
	}

	/**
	 * @return The name this player uses on the website. This is not always the same as their Minecraft username.
	 * @see #getUsername()
	 */
	public String getDisplayName() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return displayName;
	}
	
	/**
	 * @return Minecraft UUID of this player.
	 * @see #getUsername()
	 */
	public UUID getUniqueId() {
		return uuid;
	}

	/**
	 * @return A numerical group id.
	 */
	public int getGroupID() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return groupID;
	}

	/**
	 * @return The user's site reputation.
	 */
	public int getReputations() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return reputation;
	}

	/**
	 * @return The date the user registered on the website.
	 */
	public Date getRegisteredDate() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return registeredDate;
	}

	/**
	 * @return Whether an account associated with the UUID exists.
	 * @see #getUUID()
	 */
	public boolean exists() {	
		return exists;
	}

	/**
	 * @return Whether this account has been validated. An account is validated when a password is set.
	 */
	public boolean isValidated() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return validated;
	}

	/**
	 * @return Whether this account is banned from the website.
	 */
	public boolean isBanned() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return banned;
	}
	
	public List<Notification> getNotifications() throws NamelessException {
		final List<Notification> notifications = new ArrayList<>();
		
		Request request = new Request(baseUrl, Action.GET_NOTIFICATIONS, new ParameterBuilder().add("uuid", uuid).build());
		
		JsonObject object = request.getResponse();
		object.getAsJsonArray().forEach((element) -> {
			String message = element.getAsJsonObject().get("message").getAsString();
			String url = element.getAsJsonObject().get("url").getAsString();
			notifications.add(new Notification(message, url));
		});
		
		return notifications;
	}
	
	/**
	 * Sets the players group
	 * @param groupId Numerical ID associated with a group
	 * @throws NamelessException
	 */
	public void setGroup(int groupId) throws NamelessException {
		Request request = new Request(baseUrl, Action.SET_GROUP, new ParameterBuilder().add("uuid", uuid).add("group_id", groupId).build());
		request.connect();
	}
	
	/**
	 * Registers a new account. The player will be sent an email to set a password.
	 * @param minecraftName In-game name for this player
	 * @param email Email address
	 * @throws NamelessException
	 */
	public void register(String minecraftName, String email) throws NamelessException {
		String[] parameters = new ParameterBuilder().add("username", minecraftName).add("uuid", uuid).add("email", email).build();
		new Request(baseUrl, Action.REGISTER, parameters).connect();
	}

	/**
	 * Reports a player
	 * @param reportedUuid UUID of the reported player
	 * @param reportedUsername In-game name of the reported player
	 * @param reason Reason why this player has been reported
	 * @throws NamelessException
	 */
	public void createReport(UUID reportedUuid, String reportedUsername, String reason) throws NamelessException {		
		String[] parameters = new ParameterBuilder()
				.add("reporter_uuid", uuid)
				.add("reported_uuid", reportedUuid)
				.add("reported_username", reportedUsername)
				.add("reason", reason)
				.build();
		new Request(baseUrl, Action.CREATE_REPORT, parameters);
	}

}