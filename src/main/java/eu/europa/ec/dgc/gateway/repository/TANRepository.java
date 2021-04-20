package eu.europa.ec.dgc.gateway.repository;

import eu.europa.ec.dgc.gateway.entity.TANEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TANRepository extends JpaRepository<TANEntity, Long> {
}
