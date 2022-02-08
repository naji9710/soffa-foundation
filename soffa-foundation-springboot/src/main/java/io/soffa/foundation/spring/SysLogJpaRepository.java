package io.soffa.foundation.spring;

import io.soffa.foundation.spring.data.entities.SysLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysLogJpaRepository extends JpaRepository<SysLogEntity, String> {
}
