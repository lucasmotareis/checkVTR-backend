package pmto._bpm.viaturas.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.viaturas.notifications.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByBatalhaoId(Long batalhaoId);

}
