package pmto._bpm.viaturas.viaturas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.viaturas.model.ZonaViatura;

public interface ZonaViaturaRepository extends JpaRepository<ZonaViatura, Long> {

    List<ZonaViatura> findByBatalhaoIdOrderByOrdemAsc(Long batalhaoId);

    Optional<ZonaViatura> findByIdAndBatalhaoId(Long id, Long batalhaoId);

    @Query("select coalesce(max(z.ordem), 0) from ZonaViatura z where z.batalhao.id = :batalhaoId")
    Integer findMaxOrdemByBatalhaoId(@Param("batalhaoId") Long batalhaoId);
}
