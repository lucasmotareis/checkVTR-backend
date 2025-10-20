package pmto._bpm.Viaturas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmto._bpm.Viaturas.model.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByBatalhaoId(Long batalhaoId);

}
