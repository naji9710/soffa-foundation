package io.soffa.foundation.spring.config;

import io.soffa.foundation.data.SysLogRepository;
import io.soffa.foundation.data.entities.SysLog;
import io.soffa.foundation.spring.SysLogJpaRepository;
import io.soffa.foundation.spring.data.entities.SysLogEntity;
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
