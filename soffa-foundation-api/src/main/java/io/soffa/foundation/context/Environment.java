package io.soffa.foundation.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Environment {

    private static final Set<String> activeProfiles = new HashSet<>();

    private Environment() {}

    public static void addProfiles(String... profiles) {
        activeProfiles.addAll(Arrays.asList(profiles));
    }

    public static boolean hasProfile(String profile) {
        return activeProfiles.contains(profile);
    }

}
