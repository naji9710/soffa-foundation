package io.soffa.foundation.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Environment {

    private static final Set<String> PROFILES = new HashSet<>();

    private Environment() {}

    public static void addProfiles(String... profiles) {
        PROFILES.addAll(Arrays.asList(profiles));
    }

    public static boolean hasProfile(String profile) {
        return PROFILES.contains(profile);
    }

}
