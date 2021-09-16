package com.namelessmc.java_api;

public class UserFilter<FilterValue> {

	public static UserFilter<Boolean> BANNED = new UserFilter<>("banned", true);
	public static UserFilter<Boolean> UNBANNED = new UserFilter<>("banned", false);
	public static UserFilter<Boolean> VERIFIED = new UserFilter<>("verified", true);
	public static UserFilter<Boolean> UNVERIFIED = new UserFilter<>("verified", false);

	private final String filterName;
	private FilterValue value;

	public UserFilter(final String filterName, final FilterValue defaultValue) {
		this.filterName = filterName;
		this.value = defaultValue;
	}

	public void value(final FilterValue value) {
		this.value = value;
	}

	public String getName() {
		return this.filterName;
	}

	public FilterValue getValue() {
		return this.value;
	}

}
