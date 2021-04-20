package eu.europa.ec.dgc.gateway.repository;

import eu.europa.ec.dgc.gateway.entity.TanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TanRepository extends JpaRepository<TanEntity, Long> {
}
