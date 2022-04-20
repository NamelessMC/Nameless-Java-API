package com.namelessmc.java_api;

import org.checkerframework.checker.nullness.qual.NonNull;

public class UserFilter<FilterValueType> {

	public static UserFilter<Boolean> BANNED = new UserFilter<>("banned");
	public static UserFilter<Boolean> VERIFIED = new UserFilter<>("verified");
	public static UserFilter<Integer> GROUP_ID = new UserFilter<>("group_id");
	public static UserFilter<String> INTEGRATION = new UserFilter<>("integration");

	private final @NonNull String filterName;

	public UserFilter(final @NonNull String filterName) {
		this.filterName = filterName;
	}

	public @NonNull String getName() {
		return this.filterName;
	}

}
