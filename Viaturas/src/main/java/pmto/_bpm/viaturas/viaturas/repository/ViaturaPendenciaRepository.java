package pmto._bpm.viaturas.viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.viaturas.viaturas.dto.StatusPendenciaViatura;
import pmto._bpm.viaturas.viaturas.model.ViaturaPendencia;

import java.util.List;
import java.util.Optional;

public interface ViaturaPendenciaRepository extends JpaRepository<ViaturaPendencia, Long> {

    Optional<ViaturaPendencia> findFirstByViaturaIdAndProblemaIdAndStatusIn(
            Long viaturaId,
            Long problemaId,
            List<StatusPendenciaViatura> status
    );

    List<ViaturaPendencia> findByViaturaIdAndStatusInOrderByUltimaOcorrenciaEmDesc(
            Long viaturaId,
            List<StatusPendenciaViatura> status
    );

}
