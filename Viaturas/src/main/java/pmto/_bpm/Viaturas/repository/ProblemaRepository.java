package pmto._bpm.Viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.Viaturas.model.Problema;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {
}
