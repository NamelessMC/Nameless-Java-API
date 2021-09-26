package com.namelessmc.java_api;

public class UserFilter<FilterValueType> {

	public static UserFilter<Boolean> BANNED = new UserFilter<>("banned", true);
	public static UserFilter<Boolean> UNBANNED = new UserFilter<>("banned", false);
	public static UserFilter<Boolean> VERIFIED = new UserFilter<>("verified", true);
	public static UserFilter<Boolean> UNVERIFIED = new UserFilter<>("verified", false);
	public static UserFilter<Boolean> DISCORD_LINKED = new UserFilter<>("discord_linked", true);
	public static UserFilter<Boolean> DISCORD_UNLINKED = new UserFilter<>("discord_linked", false);

	private final String filterName;
	private FilterValueType value;

	public UserFilter(final String filterName, final FilterValueType defaultValue) {
		this.filterName = filterName;
		this.value = defaultValue;
	}

	public void value(final FilterValueType value) {
		this.value = value;
	}

	public String getName() {
		return this.filterName;
	}

	public FilterValueType getValue() {
		return this.value;
	}

}
