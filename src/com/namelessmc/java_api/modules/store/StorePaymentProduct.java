package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;

public class StorePaymentProduct {

	private final int id;
	private final String name;

	StorePaymentProduct(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.name = json.get("name").getAsString();
	}

	public int id() {
		return id;
	}

	public String name() {
		return name;
	}

}
