package io.soffa.foundation.spring.state;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.core.exceptions.TechnicalException;
import lombok.Data;
import lombok.SneakyThrows;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Data
@Component
public class DatabasePlane {

    public static final String READY = "READY";
    public static final String PENDING = "PENDING";
    public static final String FAILED = "FAILED";

    private String state;
    private String message;
    private LockProvider lockProvider;

    public boolean isReady() {
        return Objects.equals(state, READY);
    }

    public String getMessage() {
        if (TextUtil.isEmpty(message)) {
            return state;
        }
        return message;
    }

    public boolean isPending() {
        return Objects.equals(state, PENDING);
    }

    public boolean isFailed() {
        return Objects.equals(state, FAILED);
    }

    public void setPending() {
        state = PENDING;
        message = "";
    }

    public void setReady() {
        state = READY;
        message = "";
    }


    public void setLockProvider(LockProvider lp) {
        this.lockProvider = lp;
    }

    public void setFailed(String message) {
        state = FAILED;
        this.message = message;
    }

    public void withLock(String name, int atMostSeconds, int atLeastSeconds, Runnable runnable) {
        withLock(name, Duration.ofSeconds(atMostSeconds), Duration.ofSeconds(atLeastSeconds), runnable);
    }

    public void withLock(String name, Duration atMost, Duration atLeast, Runnable runnable) {
        LockConfiguration config = new LockConfiguration(Instant.now(), name, atMost, atLeast);
        lockProvider.lock(config).ifPresent(simpleLock -> {
            try {
                runnable.run();
            } finally {
                simpleLock.unlock();
            }
        });
    }

    @SneakyThrows
    public void await() {
        await(Duration.ofSeconds(2));
    }

    @SneakyThrows
    public void await(Duration atMost) {
        Duration duration = atMost;
        while (!isReady()) {
            //noinspection BusyWait
            Thread.sleep(500);
            if (isReady()) {
                return;
            }
            duration = duration.minus(Duration.ofMillis(500));
            if (duration.isZero() || duration.isNegative()) {
                break;
            }
        }
        if (isReady()) {
            return;
        }
        throw new TechnicalException("Timeout while waiting for database plane to be ready");
    }

}
