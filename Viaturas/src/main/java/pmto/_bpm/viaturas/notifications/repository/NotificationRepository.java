package pmto._bpm.viaturas.notifications.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pmto._bpm.viaturas.notifications.dto.NotificacaoStatsDTO;
import pmto._bpm.viaturas.notifications.model.Notification;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByBatalhaoId(Long batalhaoId, Pageable pageable);

    long countByBatalhaoId(Long batalhaoId);

    long countByBatalhaoIdAndDataCriacaoAfter(Long batalhaoId, Instant lastSeenAt);

    @Query("""
    select new pmto._bpm.viaturas.notifications.dto.NotificacaoStatsDTO(
        count(n.id),
        sum(case when n.dataCriacao >= :startOfDay then 1 else 0 end),
        sum(case when n.dataCriacao >= :startOfWeek then 1 else 0 end),
        sum(case when n.dataCriacao >= :startOfMonth then 1 else 0 end),
        max(n.dataCriacao)
    )
    from Notification n
    where n.batalhao.id = :batalhaoId
""")
    NotificacaoStatsDTO getStats(
            @Param("batalhaoId") Long batalhaoId,
            @Param("startOfDay") Instant startOfDay,
            @Param("startOfWeek") Instant startOfWeek,
            @Param("startOfMonth") Instant startOfMonth
    );


}
