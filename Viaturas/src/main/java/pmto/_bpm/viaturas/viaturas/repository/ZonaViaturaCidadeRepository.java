package pmto._bpm.viaturas.viaturas.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.viaturas.model.ZonaViaturaCidade;

public interface ZonaViaturaCidadeRepository extends JpaRepository<ZonaViaturaCidade, Long> {

    @Query("""
    select zc
    from ZonaViaturaCidade zc
    where zc.zona.batalhao.id = :batalhaoId
    order by zc.zona.ordem asc, zc.ordem asc
    """)
    List<ZonaViaturaCidade> findByBatalhaoIdOrderByZonaOrdemAscOrdemAsc(
            @Param("batalhaoId") Long batalhaoId
    );

    @Query("""
    select zc
    from ZonaViaturaCidade zc
    where zc.id = :id
      and zc.zona.batalhao.id = :batalhaoId
    """)
    Optional<ZonaViaturaCidade> findByIdAndBatalhaoId(
            @Param("id") Long id,
            @Param("batalhaoId") Long batalhaoId
    );

    @Query("select coalesce(max(zc.ordem), 0) from ZonaViaturaCidade zc where zc.zona.id = :zonaId")
    Integer findMaxOrdemByZonaId(@Param("zonaId") Long zonaId);
}
