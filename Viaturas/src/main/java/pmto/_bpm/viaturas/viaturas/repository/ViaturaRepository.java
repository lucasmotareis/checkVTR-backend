package pmto._bpm.viaturas.viaturas.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.viaturas.dto.ViaturaComBadgeDTO;
import pmto._bpm.viaturas.viaturas.dto.ViaturaQuadroItemDTO;
import pmto._bpm.viaturas.viaturas.model.Viatura;
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

    @Query("""
    select new pmto._bpm.viaturas.viaturas.dto.ViaturaQuadroItemDTO(
        v.id, v.prefixo, v.modelo, v.placa, v.manutencao, v.kmAtual, v.kmRevisao, v.combustivelAtualPercentual,
        count(c.id), z.id, zc.id
    )
    from Viatura v
    left join v.zona z
    left join v.zonaCidade zc
    left join CheckList c
        on c.viatura.id = v.id
       and c.vistoPeloChefe = false
    where v.batalhao.id = :batalhaoId
    group by v.id, v.prefixo, v.modelo, v.placa, v.manutencao, v.kmAtual, v.kmRevisao, v.combustivelAtualPercentual, z.id, zc.id
    order by v.prefixo asc
    """)
    List<ViaturaQuadroItemDTO> findViaturasParaQuadro(
            @Param("batalhaoId") Long batalhaoId
    );

    @Modifying
    @Query("update Viatura v set v.zona = null, v.zonaCidade = null where v.zona.id = :zonaId")
    int clearZonaByZonaId(@Param("zonaId") Long zonaId);

    @Modifying
    @Query("update Viatura v set v.zonaCidade = null where v.zonaCidade.id = :zonaCidadeId")
    int clearZonaCidadeByZonaCidadeId(@Param("zonaCidadeId") Long zonaCidadeId);

}
