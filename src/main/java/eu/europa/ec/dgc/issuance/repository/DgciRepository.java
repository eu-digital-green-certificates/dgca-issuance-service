package eu.europa.ec.dgc.issuance.repository;

import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DgciRepository extends JpaRepository<DgciEntity, Long> {
}
