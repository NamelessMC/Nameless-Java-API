package com.namelessmc.java_api.modules.websend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.RequestHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class WebsendAPI {

	private final @NonNull RequestHandler requests;

	public WebsendAPI(@NonNull RequestHandler requests) {
		this.requests = Objects.requireNonNull(requests, "Request handler is null");
	}

	public @NonNull List<WebsendCommand> getCommands(int serverId) throws NamelessException {
		JsonObject response = this.requests.get("websend/commands","server_id", serverId);
		JsonArray commandsJson = response.getAsJsonArray("commands");
		List<WebsendCommand> commands = new ArrayList<>(commandsJson.size());
		for (JsonElement e : commandsJson) {
			JsonObject commandJson = e.getAsJsonObject();
			int commandId = commandJson.get("id").getAsInt();
			String commandLine = commandJson.get("command").getAsString();
			commands.add(new WebsendCommand(commandId, commandLine));
		}
		return Collections.unmodifiableList(commands);
	}

	public void sendConsoleLog(int serverId, @NonNull Collection<String> lines) throws NamelessException {
		JsonObject body = new JsonObject();
		body.addProperty("server_id", serverId);
		JsonArray content = new JsonArray();
		for (String line : lines) {
			content.add(line);
		}
		body.add("content", content);
		this.requests.post("websend/console", body);
	}

}
