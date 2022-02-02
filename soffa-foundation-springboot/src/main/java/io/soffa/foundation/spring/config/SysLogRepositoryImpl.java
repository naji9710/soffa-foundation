package io.soffa.foundation.spring.config;

import io.soffa.foundation.data.SysLog;
import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.data.entities.SysLogEntity;
import io.soffa.foundation.spring.SysLogJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class SysLogRepositoryImpl implements SysLogRepository {

    private final SysLogJpaRepository repo;

    @Override
    @Transactional
    public void save(SysLog log) {
        repo.save(SysLogEntity.of(log));
    }

    @Override
    public long count() {
        return repo.count();
    }
}
