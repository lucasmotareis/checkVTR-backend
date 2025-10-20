package pmto._bpm.Viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.Viaturas.model.Viatura;

import java.util.List;

public interface ViaturaRepository extends JpaRepository<Viatura,Long> {
    List<Viatura> findByBatalhaoId(Long batalhaoId);

}
