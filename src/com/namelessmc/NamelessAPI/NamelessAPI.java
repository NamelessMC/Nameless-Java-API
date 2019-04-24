package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Request.Action;
import com.namelessmc.NamelessAPI.Website.Update;

public final class NamelessAPI {

	public static boolean DEBUG_MODE = false;
	
	private URL apiUrl;
	
	/**
	 * 
	 * @param apiUrl URL of API to connect to
	 * @param debug If debug is set to true, debug messages are enabled for <i>every</i> NamelessAPI instance.
	 */
	public NamelessAPI(URL apiUrl, boolean debug) {
		if (debug) DEBUG_MODE = true;
		this.apiUrl = apiUrl;
	}
	
	/**
	 * Checks if a web API connection can be established
	 * @return 
	 * <ul>
	 *   <li>An {@link ApiError} if the api has returned an error</li>
	 *   <li>A {@link NamelessException} if the connection was unsuccessful</li>
	 *   <li>null if the connection was successful.</li>
	 * </ul>
	 */
	public NamelessException checkWebAPIConnection() {		
		try {
			Request request = new Request(apiUrl, Action.INFO);
			request.connect();
			
			if (request.hasError()) throw new ApiError(request.getError());
			
			JsonObject response = request.getResponse();
			if (response.has("nameless_version")) {
				return null;
			} else {
				return new NamelessException("Invalid respose: " + response.getAsString());
			}
		} catch (NamelessException e) {
			return e;
		}
	}

	/**
	 * Get all announcements
	 * @return list of current announcements
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements() throws NamelessException {
		Request request = new Request(apiUrl, Action.GET_ANNOUNCEMENTS);
		request.connect();
		
		if (request.hasError()) throw new ApiError(request.getError());
		
		List<Announcement> announcements = new ArrayList<>();
		
		JsonObject object = request.getResponse();
		object.getAsJsonArray().forEach((element) -> {
			JsonObject announcementJson = element.getAsJsonObject();
			String content = announcementJson.get("content").getAsString();
			String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});
		
		return announcements;
	}
	
	/**
	 * Get all announcements visible for the player with the specified uuid
	 * @param uuid UUID of player to get visibile announcements for
	 * @return list of current announcements visible to the player
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements(UUID uuid) throws NamelessException {
		Request request = new Request(apiUrl, Action.GET_ANNOUNCEMENTS, new ParameterBuilder().add("uuid", uuid).build());
		request.connect();
		
		if (request.hasError()) throw new ApiError(request.getError());
		
		List<Announcement> announcements = new ArrayList<>();
		
		request.getResponse().get("announcements").getAsJsonArray().forEach((element) -> {
			JsonObject announcementJson = element.getAsJsonObject();
			String content = announcementJson.get("content").getAsString();
			String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});
		
		return announcements;
	}
	
	public void submitServerInfo(String jsonData) throws NamelessException {
		Request request = new Request(apiUrl, Action.SERVER_INFO, new ParameterBuilder().add("info", jsonData).build());
		request.connect();
		if (request.hasError()) throw new ApiError(request.getError());
	}
	
	public Website getWebsite() throws NamelessException {
		Request request = new Request(apiUrl, Action.INFO);
		request.connect();
		
		if (request.hasError()) throw new ApiError(request.getError());
		
		JsonObject json = request.getResponse();
		
		String version = json.get("nameless_version").getAsString();
		
		String[] modules = jsonToArray(json.get("modules").getAsJsonArray());
		
		JsonObject updateJson = json.get("version_update").getAsJsonObject();
		boolean updateAvailable = updateJson.get("update").getAsBoolean();
		Update update;
		if (updateAvailable) {
			String updateVersion = updateJson.get("version").getAsString();
			boolean isUrgent = updateJson.get("urgent").getAsBoolean();
			update = new Update(isUrgent, updateVersion);
		} else {
			update = null;
		}

		return new Website(version, update, modules);
		
	}
	
	public boolean validateUser(UUID uuid, String code) throws NamelessException {
		String[] parameters = new ParameterBuilder().add("uuid", uuid.toString()).add("code", code).build();
		Request request = new Request(apiUrl, Action.VALIDATE_USER, parameters);
		request.connect();
		if (request.hasError()) {
			int errorCode = request.getError();
			if (errorCode == 28) {
				return false;
			}
			throw new ApiError(errorCode);
		}
		return true;
	}
	
	public NamelessPlayer getPlayer(UUID uuid) throws NamelessException {
		return new NamelessPlayer(uuid, apiUrl);
	}
	
	public Map<UUID, String> getRegisteredUsers() throws NamelessException {
		Request request = new Request(apiUrl, Action.LIST_USERS);
		request.connect();
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
		
		Map<UUID, String> users = new HashMap<>();
		
		request.getResponse().get("users").getAsJsonArray().forEach(userJsonElement -> {
			final String uuid = userJsonElement.getAsJsonObject().get("uuid").getAsString();
			final String username = userJsonElement.getAsJsonObject().get("username").getAsString();
			
			users.put(websiteUuidToJavaUuid(uuid), username);
		});
		
		return users;
	}
	
	public List<NamelessPlayer> getRegisteredUsersAsNamelessPlayerList() throws NamelessException {
		Map<UUID, String> users = getRegisteredUsers();
		List<NamelessPlayer> namelessPlayers = new ArrayList<>();
		
		for (UUID userUuid : users.keySet()) {
			namelessPlayers.add(this.getPlayer(userUuid));
		}
		
		return namelessPlayers;
	}
	
	static String encode(Object object) {
		try {
			return URLEncoder.encode(object.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	static String[] jsonToArray(JsonArray jsonArray) {
		List<String> list = new ArrayList<>();
		jsonArray.iterator().forEachRemaining((element) -> list.add(element.getAsString()));
		return list.toArray(new String[] {});
	}
	
	static UUID websiteUuidToJavaUuid(String uuid) {
		// Add dashes to uuid
		// https://bukkit.org/threads/java-adding-dashes-back-to-minecrafts-uuids.272746/
		StringBuffer sb = new StringBuffer(uuid);
		sb.insert(8, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(13, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(18, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(23, "-");
		 
		return UUID.fromString(sb.toString());
	}


}