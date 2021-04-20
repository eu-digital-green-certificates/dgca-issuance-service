package eu.europa.ec.dgc.gateway.repository;

import eu.europa.ec.dgc.gateway.entity.DgciEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DgciRepository extends JpaRepository<DgciEntity, Long> {
}
