package io.soffa.foundation.commons;

import io.soffa.foundation.core.exceptions.FunctionalException;

import java.util.function.Supplier;

public class RuleManager {

    private static final RuleManager INSTANCE = new RuleManager();

    public static RuleManager getInstance() {
        return INSTANCE;
    }

    public void check(String message, Supplier<Boolean> supplier) {
        if (!supplier.get()) {
            throw new FunctionalException(message);
        }
    }
}
