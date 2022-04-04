package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

public class UserFilter<FilterValueType> {

	public static UserFilter<Boolean> BANNED = new UserFilter<>("banned");
	public static UserFilter<Boolean> VERIFIED = new UserFilter<>("verified");
	public static UserFilter<Integer> GROUP_ID = new UserFilter<>("group_id");
	public static UserFilter<String> INTEGRATION = new UserFilter<>("integration");

	private final @NotNull String filterName;

	public UserFilter(final @NotNull String filterName) {
		this.filterName = filterName;
	}

	public @NotNull String getName() {
		return this.filterName;
	}

}
