package pmto._bpm.viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.viaturas.model.Problema;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {
}
