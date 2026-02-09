package pmto._bpm.viaturas.checklists.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.viaturas.checklists.model.Problema;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {
}
