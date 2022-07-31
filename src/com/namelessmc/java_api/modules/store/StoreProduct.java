package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;

public class StoreProduct {

	private final int id;
	private final int categoryId;
	private final String name;
	private final float price;
	private final int priceCents;
	private final boolean hidden;
	private final boolean disabled;

	StoreProduct(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.categoryId = json.get("category_id").getAsInt();
		this.name = json.get("name").getAsString();
		this.price = json.get("price").getAsFloat();
		if (json.has("price_cents")) {
			this.priceCents = json.get("price_cents").getAsInt();
		} else {
			// Old module version that does not send cents yet
			this.priceCents = (int) this.price;
		}
		this.hidden = json.get("hidden").getAsBoolean();
		this.disabled = json.get("disabled").getAsBoolean();
	}

	public int id() {
		return this.id;
	}

	public int categoryId() {
		return this.categoryId;
	}

	public String name() {
		return this.name;
	}

	@Deprecated
	public float price() {
		return this.price;
	}

	public int priceCents() {
		return this.priceCents;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public boolean isDisabled() {
		return this.disabled;
	}
}
