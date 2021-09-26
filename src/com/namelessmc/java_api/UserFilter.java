package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

public class UserFilter<FilterValueType> {

	public static UserFilter<Boolean> BANNED = new UserFilter<>("banned", true);
	public static UserFilter<Boolean> UNBANNED = new UserFilter<>("banned", false);
	public static UserFilter<Boolean> VERIFIED = new UserFilter<>("verified", true);
	public static UserFilter<Boolean> UNVERIFIED = new UserFilter<>("verified", false);
	public static UserFilter<Boolean> DISCORD_LINKED = new UserFilter<>("discord_linked", true);
	public static UserFilter<Boolean> DISCORD_UNLINKED = new UserFilter<>("discord_linked", false);

	@NotNull
	private final String filterName;
	@NotNull
	private FilterValueType value;

	public UserFilter(@NotNull final String filterName, @NotNull final FilterValueType defaultValue) {
		this.filterName = filterName;
		this.value = defaultValue;
	}

	@Deprecated
	public void value(@NotNull final FilterValueType value) {
		this.value = value;
	}

	@NotNull
	public String getName() {
		return this.filterName;
	}

	@NotNull
	public FilterValueType getValue() {
		return this.value;
	}

}
