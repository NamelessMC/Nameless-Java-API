package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

public class UserFilter<FilterValueType> {

	public static UserFilter<Boolean> BANNED = new UserFilter<>("banned", true);
	public static UserFilter<Boolean> UNBANNED = new UserFilter<>("banned", false);
	public static UserFilter<Boolean> VERIFIED = new UserFilter<>("verified", true);
	public static UserFilter<Boolean> UNVERIFIED = new UserFilter<>("verified", false);
	public static UserFilter<Boolean> DISCORD_LINKED = new UserFilter<>("discord_linked", true);
	public static UserFilter<Boolean> DISCORD_UNLINKED = new UserFilter<>("discord_linked", false);

	private final @NotNull String filterName;
	private final @NotNull FilterValueType value;

	public UserFilter(final @NotNull String filterName, final @NotNull FilterValueType defaultValue) {
		this.filterName = filterName;
		this.value = defaultValue;
	}

	public @NotNull String getName() {
		return this.filterName;
	}

	public @NotNull FilterValueType getValue() {
		return this.value;
	}

}
