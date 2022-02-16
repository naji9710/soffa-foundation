package io.soffa.foundation.commons;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class Properties {

    private Map<String, String> internal;

    public boolean has(String name) {
        return internal.containsKey(name);
    }

    public String get(String name, String defaultValue) {
        if (!has(name)) {
            return defaultValue;
        }
        return internal.get(name);
    }

    public int getInt(String name, int defaultValue) {
        if (!has(name)) {
            return defaultValue;
        }
        return Integer.parseInt(internal.get(name));
    }
}
