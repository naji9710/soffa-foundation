package io.soffa.foundation.spring.controllers;

import io.soffa.foundation.spring.state.DatabasePlane;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("DbMigration")
@AllArgsConstructor
public class DatabaseMigrationHealthIndicator implements HealthIndicator {

    private final DatabasePlane state;

    @Override
    public Health health() {
        if (state.isReady()) {
            return Health.up().build();
        }
        Health.Builder status = Health.down();
        status.withDetail("message", state.getMessage());
        return status.build();
    }

}
