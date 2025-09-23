package pmto._bpm.Viaturas.service;

import org.springframework.stereotype.Service;
import pmto._bpm.Viaturas.model.Notification;
import pmto._bpm.Viaturas.dto.NotificationDTO;
import pmto._bpm.Viaturas.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification criar(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setTitulo(dto.getTitulo());
        notification.setDescricao(dto.getDescricao());
        return notificationRepository.save(notification);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public void deletar(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Notificação com ID " + id + " não encontrada.");
        }
    }

    public Notification atualizar (Long id, NotificationDTO dto) {
        Notification existente = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada com ID: " + id));
        existente.setTitulo(dto.getTitulo());
        existente.setDescricao(dto.getDescricao());
        return notificationRepository.save(existente);
    }

}
