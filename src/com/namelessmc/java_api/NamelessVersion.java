package com.namelessmc.java_api;

import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public enum NamelessVersion {

	V2_0_0_PR_7("2.0.0-pr7", "2.0.0 pre-release 7", 2, 0,  true),
	V2_0_0_PR_8("2.0.0-pr8", "2.0.0 pre-release 8", 2, 0, true),
	V2_0_0_PR_9("2.0.0-pr9", "2.0.0 pre-release 9", 2, 0, true),
	V2_0_0_PR_10("2.0.0-pr10", "2.0.0 pre-release 10", 2, 0, true),
	V2_0_0_PR_11("2.0.0-pr11", "2.0.0 pre-release 11", 2, 0, true),
	V2_0_0_PR_12("2.0.0-pr12", "2.0.0 pre-release 12", 2, 0, true),
	V2_0_0_PR_13("2.0.0-pr13", "2.0.0 pre-release 13", 2, 0, true),
	V2_0(null, "2.0.*", 2, 0, false),

	;

	private static final Set<NamelessVersion> SUPPORTED_VERSIONS = EnumSet.of(
			V2_0_0_PR_13,
			V2_0
	);

	private final @Nullable String name; // Only for pre-releases that use literal name matching
	private final @NonNull String friendlyName;
	private final int major;
	private final int minor;
	private final boolean preRelease;

	@SuppressWarnings("SameParameterValue")
	NamelessVersion(final @Nullable String name,
					final @NonNull String friendlyName,
					final int major,
					final int minor,
					final boolean preRelease) {
		this.name = name;
		this.friendlyName = friendlyName;
		this.major = major;
		this.minor = minor;
		this.preRelease = preRelease;
	}

	public @Nullable String preReleaseName() {
		return this.name;
	}

	public @NonNull String friendlyName() {
		return this.friendlyName;
	}

	public int major() {
		return this.major;
	}

	public int minor() {
		return this.minor;
	}

	/**
	 * @return True if this version is a pre-release
	 */
	public boolean isPreRelease() {
		return this.preRelease;
	}

	@Override
	public String toString() {
		return this.friendlyName;
	}

	private static final Map<String, NamelessVersion> BY_NAME = new HashMap<>();

	private static final NamelessVersion[] CACHED_VALUES = NamelessVersion.values();

	static {
		for (final NamelessVersion version : allVersions()) {
			if (version.isPreRelease()) {
				BY_NAME.put(version.preReleaseName(), version);
			}
		}
	}

	public static NamelessVersion parse(final @NonNull String versionName) throws UnknownNamelessVersionException {
		Objects.requireNonNull(versionName, "Version name is null");
		if (versionName.contains("-pr")) {
			// Pre-release version should match exactly
			NamelessVersion version = BY_NAME.get(versionName);
			if (version == null) {
				throw new UnknownNamelessVersionException(versionName, "no pre-release matches exactly");
			}
			return version;
		}
		String[] split = versionName.split("\\.");
		if (split.length != 3) {
			throw new UnknownNamelessVersionException(versionName, "version doesn't split to 3 components");
		}
		int[] splitInts = new int[3];
		for (int i = 0; i < 3; i++) {
			try {
				splitInts[i] = Integer.parseInt(split[i]);
			} catch (NumberFormatException e) {
				throw new UnknownNamelessVersionException(versionName, "split component " + i + " is not an integer");
			}
		}

		int major = splitInts[0];
		int minor = splitInts[1];

		for (NamelessVersion version : allVersions()) {
			if (version.major == major && version.minor == minor) {
				return version;
			}
		}

		throw new UnknownNamelessVersionException(versionName, "no match for major=" + major + " minor=" + minor);

	}

	public static NamelessVersion[] allVersions() {
		return CACHED_VALUES;
	}

	/**
	 * @return List of NamelessMC versions supported by the Java API
	 */
	public static Set<NamelessVersion> supportedVersions() {
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
