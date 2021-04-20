package eu.europa.ec.dgc.gateway.repository;

import eu.europa.ec.dgc.gateway.entity.DGCIEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DGCIRepository extends JpaRepository<DGCIEntity, Long> {
}
