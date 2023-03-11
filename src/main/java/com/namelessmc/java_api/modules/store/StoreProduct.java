package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.util.GsonHelper;

import java.util.List;

public class StoreProduct {

	private final int id;
	private final int categoryId;
	private final String name;
	private final int priceCents;
	private final boolean hidden;
	private final boolean disabled;
	private final int[] requiredProductsIds;
	private final int[] requiredGroupsIds;
	private final int[] requiredIntegrationsIds;
	private final String descriptionHtml;
	private final List<StoreProductField> fields;
	private final List<StoreProductAction> actions;

	StoreProduct(JsonObject json) {
		if (!json.has("price_cents")) {
			throw new IllegalArgumentException("Missing price_cents, are you using an old store module version?");
		}
		this.id = json.get("id").getAsInt();
		this.categoryId = json.get("category_id").getAsInt();
		this.name = json.get("name").getAsString();
		this.priceCents = json.get("price_cents").getAsInt();
		this.hidden = json.get("hidden").getAsBoolean();
		this.disabled = json.get("disabled").getAsBoolean();
		this.requiredProductsIds = GsonHelper.toIntArray(json.getAsJsonArray("required_products"));
		this.requiredGroupsIds = GsonHelper.toIntArray(json.getAsJsonArray("required_groups"));
		this.requiredIntegrationsIds = GsonHelper.toIntArray(json.getAsJsonArray("required_integrations"));
		this.descriptionHtml = json.get("description").getAsString();
		this.fields = GsonHelper.toObjectList(json.getAsJsonArray("fields"), StoreProductField::new);
		this.actions = GsonHelper.toObjectList(json.getAsJsonArray("actions"), StoreProductAction::new);
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

	public int priceCents() {
		return this.priceCents;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	public int[] requiredProductsIds() {
		return this.requiredProductsIds;
	}

	public int[] requiredGroupsIds() {
		return this.requiredGroupsIds;
	}

	public int[] requiredIntegrationsIds() {
		return this.requiredIntegrationsIds;
	}

	public String descriptionHtml() {
		return this.descriptionHtml;
	}

	public List<StoreProductField> fields() {
		return this.fields;
	}

	public List<StoreProductAction> actions() {
		return this.actions;
	}

}
