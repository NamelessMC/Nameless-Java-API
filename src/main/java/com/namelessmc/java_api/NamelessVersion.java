package com.namelessmc.java_api;

import java.util.HashMap;
import java.util.Map;

import com.namelessmc.java_api.exception.UnknownNamelessVersionException;

public enum NamelessVersion {

	V2_0_0_PR_7("2.0.0-pr7", 2, 0, true),
	V2_0_0_PR_8("2.0.0-pr8", 2, 0, true),
	V2_0_0_PR_9("2.0.0-pr9", 2, 0, true),
	V2_0_0_PR_10("2.0.0-pr10", 2, 0, true),
	V2_0_0_PR_11("2.0.0-pr11", 2, 0, true),
	V2_0_0_PR_12("2.0.0-pr12", 2, 0, true),

	;

	private String name;
	private int major;
	private int minor;
	private boolean isBeta;

	NamelessVersion(final String name, final int major, final int minor, final boolean isBeta) {
		this.name = name;
		this.major = major;
		this.minor = minor;
		this.isBeta = isBeta;
	}

	public String getName() {
		return this.name;
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
		return this.getName();
	}

	private static final Map<String, NamelessVersion> BY_NAME = new HashMap<>();

	static {
		for (final NamelessVersion version : values()) {
			BY_NAME.put(version.getName(), version);
		}
	}

	public static NamelessVersion parse(final String versionName) throws UnknownNamelessVersionException {
		final NamelessVersion version = BY_NAME.get(versionName);
		if (version == null) {
			throw new UnknownNamelessVersionException(versionName);
		}
		return version;
	}

}
