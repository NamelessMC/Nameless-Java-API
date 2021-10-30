package com.namelessmc.java_api;

import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum NamelessVersion {

	V2_0_0_PR_7("2.0.0-pr7", "2.0.0 pre-release 7", 2, 0, true),
	V2_0_0_PR_8("2.0.0-pr8", "2.0.0 pre-release 8", 2, 0, true),
	V2_0_0_PR_9("2.0.0-pr9", "2.0.0 pre-release 9", 2, 0, true),
	V2_0_0_PR_10("2.0.0-pr10", "2.0.0 pre-release 10", 2, 0, true),
	V2_0_0_PR_11("2.0.0-pr11", "2.0.0 pre-release 11", 2, 0, true),
	V2_0_0_PR_12("2.0.0-pr12", "2.0.0 pre-release 12", 2, 0, true),
	V2_0_0_PR_13("2.0.0-pr13", "2.0.0 pre-release 13", 2, 0, true),

	;

	@NotNull
	private final String name;
	private final String friendlyName;
	private final int major;
	private final int minor;
	private final boolean isBeta;

	NamelessVersion(@NotNull final String name, String friendlyName, final int major, final int minor, final boolean isBeta) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.major = major;
		this.minor = minor;
		this.isBeta = isBeta;
	}

	public @NotNull String getName() {
		return this.name;
	}

	public @NotNull String getFriendlyName() {
		return this.friendlyName;
	}

	public int getMajor() {
		return this.major;
	}

	public int getMinor() {
		return this.minor;
	}

	/**
	 * @return True if this version is a release candidate, pre-release, beta, alpha.
	 */
	public boolean isBeta() {
		return this.isBeta;
	}

	@Override
	public String toString() {
		return this.friendlyName;
	}

	private static final Map<String, NamelessVersion> BY_NAME = new HashMap<>();

	static {
		for (final NamelessVersion version : values()) {
			BY_NAME.put(version.getName(), version);
		}
	}

	@NotNull
	public static NamelessVersion parse(@NotNull final String versionName) throws UnknownNamelessVersionException {
		final NamelessVersion version = BY_NAME.get(versionName);
		if (version == null) {
			throw new UnknownNamelessVersionException(versionName);
		}
		return version;
	}

}
