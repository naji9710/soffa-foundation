package app.gateways.outbound;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SysLogJpaRepository extends JpaRepository<SysLogEntity, String> {
}
