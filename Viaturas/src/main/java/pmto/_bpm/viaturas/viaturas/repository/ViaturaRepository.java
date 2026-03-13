package pmto._bpm.viaturas.viaturas.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.checklists.model.CheckList;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ViaturaRepository extends JpaRepository<Viatura,Long> {
    List<Viatura> findByBatalhaoId(Long batalhaoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Viatura v where v.id = :id")
    Optional<Viatura> findByIdForUpdate(@Param("id") Long id);


    @Query("""
    select new pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO(
        v.id, v.prefixo, v.modelo, v.placa,v.manutencao, v.kmAtual,v.kmRevisao, v.combustivelAtualPercentual,
        count(c.id)
    )
    from Viatura v
    left join CheckList c
        on c.viatura.id = v.id
       and c.vistoPeloChefe = false
    where v.batalhao.id = :batalhaoId
    group by v.id, v.prefixo, v.modelo, v.placa, v.manutencao,v.kmAtual,v.kmRevisao,v.combustivelAtualPercentual
    order by v.prefixo asc
    """)
    List<ViaturaComBadgeDTO> findViaturasComBadge(
            @Param("batalhaoId") Long batalhaoId
    );

}
