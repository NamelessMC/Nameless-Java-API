package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;

public class StoreProduct {

	private final int id;
	private final int categoryId;
	private final String name;
	private final float price;
	private final int order;
	private final boolean hidden;
	private final boolean disabled;

	StoreProduct(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.categoryId = json.get("category_id").getAsInt();
		this.name = json.get("name").getAsString();
		this.price = json.get("price").getAsFloat();
		this.order = json.get("order").getAsInt();
		this.hidden = json.get("hidden").getAsBoolean();
		this.disabled = json.get("disabled").getAsBoolean();
	}

	public int getId() {
		return this.id;
	}

	public int getCategoryId() {
		return this.categoryId;
	}

	public String getName() {
		return this.name;
	}

	public float getPrice() {
		return this.price;
	}

	public int getOrder() {
		return this.order;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public boolean isDisabled() {
		return this.disabled;
	}
}
