package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.util.GsonHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StoreProductField {

	private final int id;
	private final String identifier;
	private final int typeId; // TODO enum
	private final boolean required;
	private final int min;
	private final @Nullable String regex;
	private final String defaultValue;

	public StoreProductField(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.identifier = json.get("identifier").getAsString();
		this.typeId = json.get("type").getAsInt();
		this.required = json.get("required").getAsBoolean();
		this.min = json.get("min").getAsInt();
		this.regex = GsonHelper.getNullableString(json, "regex");
		this.defaultValue = json.get("default_value").getAsString();
	}

	public int id() {
		return this.id;
	}

	public String identifier() {
		return this.identifier;
	}

	@Deprecated
	public int typeId() {
		return this.typeId;
	}

	public boolean required() {
		return this.required;
	}

	public int min() {
		return this.min;
	}

	public @Nullable String regex() {
		return this.regex;
	}

	public String defaultValue() {
		return this.defaultValue;
	}

}
