package io.soffa.foundation.data;

import java.time.Duration;

public interface DatabasePlane {

    default void await() {
        await(Duration.ofSeconds(3));
    }

    void await(Duration duration);

    boolean isReady();

    String getMessage();

    void setPending();

    void setReady();

    boolean isPending();

    void setFailed(String message);
}
