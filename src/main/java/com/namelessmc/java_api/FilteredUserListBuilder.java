package com.namelessmc.java_api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.NamelessException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class FilteredUserListBuilder {

	private final @NonNull NamelessAPI api;
	private @Nullable Map<UserFilter<?>, Object> filters;
	private @NonNull String operator = "AND";

	FilteredUserListBuilder(@NonNull NamelessAPI api) {
		this.api = api;
	}

	public <T> @NonNull FilteredUserListBuilder withFilter(final @NonNull UserFilter<T> filter,
														   final @NonNull T value) {
		if (filters == null) {
			filters = new HashMap<>();
		}

		filters.put(
				Objects.requireNonNull(filter, "Filter is null"),
				Objects.requireNonNull(value, "Value for filter " + filter.name() + " is null")
		);
		return this;
	}

	public @NonNull FilteredUserListBuilder all() {
		this.operator = "AND";
		return this;
	}

	public @NonNull FilteredUserListBuilder any() {
		this.operator = "OR";
		return this;
	}

	public JsonObject makeRawRequest() throws NamelessException {
		final Object[] parameters;
		if (filters != null) {
			int filterCount = filters.size();
			parameters = new Object[2 + 4 + filterCount * 2];
			int i = 2;
			parameters[i++] = "operator";
			parameters[i++] = operator;
			parameters[i++] = "limit";
			parameters[i++] = 0;
			for (Map.Entry<UserFilter<?>, Object> filter : this.filters.entrySet()) {
				parameters[i++] = filter.getKey().name();
				parameters[i++] = filter.getValue();
			}
		} else {
			parameters = new Object[2];
		}

		parameters[0] = "groups"; // Request NamelessMC to include groups in response

		return this.api.requests().get("users", parameters);
	}

	public @NonNull List<@NonNull NamelessUser> makeRequest() throws NamelessException {
		final JsonObject response = this.makeRawRequest();
		final JsonArray array = response.getAsJsonArray("users");
		final List<NamelessUser> users = new ArrayList<>(array.size());
		for (final JsonElement e : array) {
			users.add(new NamelessUser(this.api, e.getAsJsonObject()));
		}
		return Collections.unmodifiableList(users);
	}

}
