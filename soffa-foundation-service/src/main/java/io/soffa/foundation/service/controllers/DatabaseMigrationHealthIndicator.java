package io.soffa.foundation.service.controllers;

import io.soffa.foundation.data.DatabasePlane;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

@Component("db-migration")
@AllArgsConstructor
public class DatabaseMigrationHealthIndicator implements HealthIndicator {

    private final DatabasePlane state;

    @Override
    public Health health() {
        Status status;
        if (state.isReady()) {
            status = new Status(Status.UP.getCode());
        }else {
            status = new Status(Status.DOWN.getCode(), state.getMessage());
        }
        return Health.status(status).build();
    }


}
