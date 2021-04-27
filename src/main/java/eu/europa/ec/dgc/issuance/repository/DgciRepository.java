package eu.europa.ec.dgc.issuance.repository;

import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DgciRepository extends JpaRepository<DgciEntity, Long> {
    Optional<DgciEntity> findByDgci(String dgci);
}
