package com.namelessmc.java_api.modules.websend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.NamelessModule;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WebsendAPI {

	private final RequestHandler requests;

	public WebsendAPI(final NamelessAPI api) throws NamelessException {
		this.requests = api.requests();
		api.ensureModuleInstalled(NamelessModule.WEBSEND);
	}

	public @NonNull List<WebsendCommand> commands(int serverId) throws NamelessException {
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

	public void sendConsoleLog(int serverId, Collection<String> lines) throws NamelessException {
		sendConsoleLog(serverId, lines, false);
	}

	public void sendConsoleLog(int serverId, Collection<String> lines, boolean clearPrevious) throws NamelessException {
		JsonObject body = new JsonObject();
		body.addProperty("server_id", serverId);
		body.addProperty("clear_previous", clearPrevious);
		JsonArray content = new JsonArray();
		for (String line : lines) {
			content.add(line);
		}
		body.add("content", content);
		this.requests.post("websend/console", body);
	}

}
