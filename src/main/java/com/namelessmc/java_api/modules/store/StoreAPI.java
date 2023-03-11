package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.NamelessModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StoreAPI {

	private final NamelessAPI api;
	private final RequestHandler requests;

	public StoreAPI(final NamelessAPI api) throws NamelessException {
		this.api = api;
		this.requests = api.requests();
		this.api.ensureModuleInstalled(NamelessModule.STORE);
	}

	public List<StoreProduct> products() throws NamelessException {
		JsonObject response = this.requests.get("store/products");
		JsonArray productsJson = response.getAsJsonArray("products");
		List<StoreProduct> products = new ArrayList<>(productsJson.size());
		for (JsonElement productElement : productsJson) {
			products.add(new StoreProduct(productElement.getAsJsonObject()));
		}
		return Collections.unmodifiableList(products);
	}

	public List<StorePayment> payments(PaymentsFilter... filters) throws NamelessException {
		Object[] params = new Object[filters.length * 2];
		for (int i = 0; i < filters.length; i++) {
			params[i*2] = filters[i].name();
			params[i*2+1] = filters[i].value();
		}
		JsonObject response = this.requests.get("store/payments", params);
		JsonArray paymentsJson = response.getAsJsonArray("payments");
		List<StorePayment> payments = new ArrayList<>(paymentsJson.size());
		for (JsonElement productElement : paymentsJson) {
			payments.add(new StorePayment(this.api, productElement.getAsJsonObject()));
		}
		return Collections.unmodifiableList(payments);
	}

	public List<StoreCategory> categories() throws NamelessException {
		JsonObject response = this.requests.get("store/products");
		JsonArray array = response.getAsJsonArray("categories");
		List<StoreCategory> categories = new ArrayList<>(array.size());
		for (JsonElement element : array) {
			categories.add(new StoreCategory(element.getAsJsonObject()));
		}
		return Collections.unmodifiableList(categories);
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
