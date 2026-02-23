package pmto._bpm.viaturas.analytics.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.analytics.dto.TopItemDTO;
import pmto._bpm.viaturas.analytics.dto.ViaturaCountDTO;
import pmto._bpm.viaturas.checklists.model.CheckListProblema;

import java.time.Instant;
import java.util.List;

public interface AnalyticsRepository extends Repository<CheckListProblema, Long> {
    @Query("""
        select new pmto._bpm.viaturas.analytics.dto.TopItemDTO(
            p.descricao,
            count(cp.id)
        )
        from CheckListProblema cp
        join cp.problema p
        join cp.checklist c
        join c.viatura v
        where v.batalhao.id = :batalhaoId
          and (c.data >= :inicio)
          and (c.data <= :fim)
        group by p.descricao
        order by count(cp.id) desc
    """)
    List<TopItemDTO> topProblemas(
            @Param("batalhaoId") Long batalhaoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim,
            Pageable pageable
    );

    @Query("""
        select new pmto._bpm.viaturas.analytics.dto.ViaturaCountDTO(
            v.id,
            v.prefixo,
            count(cp.id)
        )
        from CheckListProblema cp
        join cp.checklist c
        join c.viatura v
        where v.batalhao.id = :batalhaoId
          and (c.data >= :inicio)
          and ( c.data <= :fim)
        group by v.id, v.prefixo
        order by count(cp.id) desc
    """)
    List<ViaturaCountDTO> viaturasComMaisProblemas(
            @Param("batalhaoId") Long batalhaoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim,
            Pageable pageable
    );


    @Query(value = """
    select extract(year from c.data) as ano,
           extract(month from c.data) as mes,
           count(*) as total
    from checklist c
    join viatura v on v.id = c.viatura_id
    where v.batalhao_id = :batalhaoId
      and c.data >= :inicio
      and c.data < :fim
    group by extract(year from c.data), extract(month from c.data)
    order by extract(year from c.data), extract(month from c.data)
""", nativeQuery = true)
    List<Object[]> checklistsPorAnoMesRaw(
            @Param("batalhaoId") Long batalhaoId,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim
    );


}
