package app.gateways.outbound;

import io.soffa.foundation.data.SysLog;
import io.soffa.foundation.data.SysLogRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@ConditionalOnProperty(value = "app.syslogs.enabled", havingValue = "true")
public class SysLogRepositoryImpl implements SysLogRepository {

    private SysLogJpaRepository repo;

    @Override
    public void save(SysLog log) {
        SysLogEntity e = repo.save(SysLogEntity.fromDomain(log));
        log.setId(e.getId());
        log.setCreatedAt(e.getCreatedAt());
    }

    @Override
    public long count() {
        return repo.count();
    }
}
