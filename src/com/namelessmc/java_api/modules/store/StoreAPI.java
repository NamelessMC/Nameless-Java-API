package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.RequestHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StoreAPI {

	private final NamelessAPI api;
	private final RequestHandler requests;

	public StoreAPI(NamelessAPI api) {
		this.api = api;
		this.requests = api.getRequestHandler();
	}

	public List<StoreProduct> products() throws NamelessException {
		JsonObject response = this.requests.get("store/products");
		JsonArray productsJson = response.getAsJsonArray("products");
		List<StoreProduct> products = new ArrayList<>(productsJson.size());
		for (JsonElement productElement : productsJson) {
			products.add(new StoreProduct(productElement.getAsJsonObject()));
		}
		return products;
	}

	public List<StorePayment> payments() throws NamelessException {
		JsonObject response = this.requests.get("store/payments");
		JsonArray paymentsJson = response.getAsJsonArray("payments");
		List<StorePayment> payments = new ArrayList<>(paymentsJson.size());
		for (JsonElement productElement : paymentsJson) {
			payments.add(new StorePayment(this.api, productElement.getAsJsonObject()));
		}
		return payments;
	}

	public PendingCommandsResponse pendingCommands(int connectionId) throws NamelessException {
		JsonObject response = this.requests.get("store/pending-commands", "connection_id", connectionId);
		return new PendingCommandsResponse(this.api, response);
	}

	public void markCommandsExecuted(Collection<PendingCommandsResponse.PendingCommand> commands) throws NamelessException {
		JsonArray array = new JsonArray(commands.size());
		for (PendingCommandsResponse.PendingCommand command : commands) {
			array.add(command.id());
		}
		JsonObject body = new JsonObject();
		body.add("commands", array);
		this.requests.post("store/commands-executed", body);
	}

}
