package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Request.Action;
import com.namelessmc.NamelessAPI.Website.Update;

public final class NamelessAPI {

	private NamelessAPI() {}
	
	/**
	 * Checks if a web API connection can be established
	 * @return 
	 * <ul>
	 *   <li>An {@link ApiError} if the api has returned an error</li>
	 *   <li>A {@link NamelessException} if the connection was unsuccessful</li>
	 *   <li>null if the connection was successful.</li>
	 * </ul>
	 */
	public static NamelessException checkWebAPIConnection(URL url) {		
		try {
			Request request = new Request(url, Action.INFO);
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
	
	/**
	 * Get all announcements
	 * @param apiUrl
	 * @return
	 * @throws NamelessException
	 */
	public static List<Announcement> getAnnouncements(URL apiUrl) throws NamelessException {
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
	 * @param apiUrl
	 * @return
	 * @throws NamelessException
	 */
	public static List<Announcement> getAnnouncements(URL apiUrl, UUID uuid) throws NamelessException {
		Request request = new Request(apiUrl, Action.GET_ANNOUNCEMENTS, new ParameterBuilder().add("uuid", uuid).build());
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
	
	public static void submitServerInfo(URL apiUrl, String jsonData) throws NamelessException {
		Request request = new Request(apiUrl, Action.SERVER_INFO, new ParameterBuilder().add("info", jsonData).build());
		request.connect();
		if (request.hasError()) throw new ApiError(request.getError());
	}
	
	public static Website getWebsiteInfo(URL apiUrl) throws NamelessException {
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
	
	public static boolean validateUser(URL apiUrl, UUID uuid, String code) throws NamelessException {
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


}