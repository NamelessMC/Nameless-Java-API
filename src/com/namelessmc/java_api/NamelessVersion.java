package com.namelessmc.java_api;

import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public enum NamelessVersion {

	V2_0_0_PR_7("2.0.0-pr7", "2.0.0 pre-release 7", 2, 0, true),
	V2_0_0_PR_8("2.0.0-pr8", "2.0.0 pre-release 8", 2, 0, true),
	V2_0_0_PR_9("2.0.0-pr9", "2.0.0 pre-release 9", 2, 0, true),
	V2_0_0_PR_10("2.0.0-pr10", "2.0.0 pre-release 10", 2, 0, true),
	V2_0_0_PR_11("2.0.0-pr11", "2.0.0 pre-release 11", 2, 0, true),
	V2_0_0_PR_12("2.0.0-pr12", "2.0.0 pre-release 12", 2, 0, true),
	V2_0_0_PR_13("2.0.0-pr13", "2.0.0 pre-release 13", 2, 0, true),

	;

	private static final Set<NamelessVersion> SUPPORTED_VERSIONS = EnumSet.of(
			// Actually, only pr13 is supported but pr13 development releases still report as pr12
			V2_0_0_PR_12,
			V2_0_0_PR_13
	);

	private final @NonNull String name;
	private final @NonNull String friendlyName;
	private final int major;
	private final int minor;
	private final boolean isBeta;

	@SuppressWarnings("SameParameterValue")
	NamelessVersion(final @NonNull String name,
					final @NonNull String friendlyName,
					final int major,
					final int minor,
					final boolean isBeta) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.major = major;
		this.minor = minor;
		this.isBeta = isBeta;
	}

	public @NonNull String getName() {
		return this.name;
	}

	public @NonNull String getFriendlyName() {
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

	public static @NonNull NamelessVersion parse(final @NonNull String versionName) throws UnknownNamelessVersionException {
		Objects.requireNonNull(versionName, "Version name is null");
		final NamelessVersion version = BY_NAME.get(versionName);
		if (version == null) {
			throw new UnknownNamelessVersionException(versionName);
		}
		return version;
	}

	/**
	 * @return List of NamelessMC versions supported by the Java API
	 */
	public static Set<NamelessVersion> getSupportedVersions() {
		return SUPPORTED_VERSIONS;
	}

	/**
	 * @param version A version to check
	 * @return Whether the provided NamelessMC version is supported by this Java API library.
	 */
	public static boolean isSupportedByJavaApi(final NamelessVersion version) {
		return SUPPORTED_VERSIONS.contains(version);
	}

}
