package eu.europa.ec.dgc.issuance.repository;

import eu.europa.ec.dgc.issuance.entity.TanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TanRepository extends JpaRepository<TanEntity, Long> {
}
