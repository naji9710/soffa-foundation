package io.soffa.foundation.service.state;

import io.soffa.foundation.commons.TextUtil;
import io.soffa.foundation.data.DatabasePlane;
import io.soffa.foundation.errors.TechnicalException;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Data
@Component
public class DatabasePlaneImpl implements DatabasePlane {

    public static final String READY = "READY";
    public static final String PENDING = "PENDING";
    public static final String FAILED = "FAILED";

    private String state = "PENDING";
    private String message;

    @Override
    public boolean isReady() {
        return Objects.equals(state, READY);
    }

    @Override
    public String getMessage() {
        if (TextUtil.isNotEmpty(message)) {
            return message;
        }
        if (isPending()) {
            return "Pending database migrations";
        }
        if (isFailed()) {
            return "Failed database migrations";
        }
        if (isReady()) {
            return "Database migrations applied";
        }
        return state;
    }

    @Override
    public boolean isPending() {
        return Objects.equals(state, PENDING);
    }

    public boolean isFailed() {
        return Objects.equals(state, FAILED);
    }

    @Override
    public void setPending() {
        state = PENDING;
        message = "";
    }

    @Override
    public void setReady() {
        state = READY;
        message = "";
    }

    @Override
    public void setFailed(String message) {
        state = FAILED;
        this.message = message;
    }

    @SneakyThrows
    @Override
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
