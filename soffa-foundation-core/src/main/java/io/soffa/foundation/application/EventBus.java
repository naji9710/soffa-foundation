package io.soffa.foundation.application;

public final class EventBus {

    private static final com.google.common.eventbus.EventBus instance = new com.google.common.eventbus.EventBus("default");

    private EventBus() {
    }

    public static void register(Object target) {
        instance.register(target);
    }

    public static void unregister(Object target) {
        instance.unregister(target);
    }

    public static void post(Object event) {
        instance.post(event);
    }

}
