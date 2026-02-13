package pmto._bpm.viaturas.notifications.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.viaturas.notifications.model.Notification;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByBatalhaoId(Long batalhaoId, Pageable pageable);

    long countByBatalhaoId(Long batalhaoId);

    long countByBatalhaoIdAndDataCriacaoAfter(Long batalhaoId, Instant lastSeenAt);

}
