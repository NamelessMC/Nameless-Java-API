package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;

import java.util.ArrayList;
import java.util.List;

public class PendingCommandsResponse {

	private final boolean useUuids;
	private final List<PendingCommandsCustomer> customers;

	PendingCommandsResponse(NamelessAPI api, JsonObject json) {
		this.useUuids = json.get("online_mode").getAsBoolean();
		JsonArray customers = json.getAsJsonArray("customers");
		this.customers = new ArrayList<>(customers.size());
		for (JsonElement element : customers) {
			this.customers.add(new PendingCommandsCustomer(api, element.getAsJsonObject()));
		}
	}

	public boolean shouldUseUuids() {
		return this.useUuids;
	}

	public List<PendingCommandsCustomer> customers() {
		return this.customers;
	}

	public static class PendingCommandsCustomer extends StoreCustomer {

		private final List<PendingCommand> pendingCommands;

		private PendingCommandsCustomer(NamelessAPI api, JsonObject json) {
			super(api, json);

			JsonArray commands = json.getAsJsonArray("commands");
			this.pendingCommands = new ArrayList<>(commands.size());
			for (JsonElement element : commands) {
				this.pendingCommands.add(new PendingCommand(element.getAsJsonObject()));
			}
		}

		public List<PendingCommand> pendingCommands() {
			return this.pendingCommands;
		}

	}

	public static class PendingCommand {

		private final int id;
		private final String command;
		private final int orderId;
		private final boolean requireOnline;

		private PendingCommand(JsonObject json) {
			this.id = json.get("id").getAsInt();
			this.command = json.get("command").getAsString();
			this.orderId = json.get("order_id").getAsInt();
			this.requireOnline = json.get("require_online").getAsBoolean();
		}

		public int id() {
			return id;
		}

		public String command() {
			return command;
		}

		public int orderId() {
			return orderId;
		}

		public boolean isOnlineRequired() {
			return requireOnline;
		}

	}

}
