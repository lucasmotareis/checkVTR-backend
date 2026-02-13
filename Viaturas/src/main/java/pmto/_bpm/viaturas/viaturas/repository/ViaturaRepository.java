package pmto._bpm.viaturas.viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
import pmto._bpm.viaturas.checklists.model.CheckList;

import java.time.Instant;
import java.util.List;

public interface ViaturaRepository extends JpaRepository<Viatura,Long> {
    List<Viatura> findByBatalhaoId(Long batalhaoId);


    @Query("""
    select new pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO(
        v.id, v.prefixo, v.modelo, v.placa,v.manutencao,
        count(c.id)
    )
    from Viatura v
    left join CheckList c
        on c.viatura.id = v.id
       and c.data > coalesce(v.chefeChecklistVistoPorUltimo, :epoch)
    where v.batalhao.id = :batalhaoId
    group by v.id, v.prefixo, v.modelo, v.placa, v.manutencao
    order by v.prefixo asc
    """)
    List<ViaturaComBadgeDTO> findViaturasComBadge(
            @Param("batalhaoId") Long batalhaoId,
            @Param("epoch") Instant epoch
    );

}
