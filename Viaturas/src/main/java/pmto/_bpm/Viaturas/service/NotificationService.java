package pmto._bpm.Viaturas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmto._bpm.Viaturas.model.Batalhao;
import pmto._bpm.Viaturas.model.Notification;
import pmto._bpm.Viaturas.dto.NotificationDTO;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.repository.BatalhaoRepository;
import pmto._bpm.Viaturas.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    @Autowired
    private BatalhaoRepository batalhaoRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification criar(NotificationDTO dto) {
        Batalhao batalhao = batalhaoRepository.findById(dto.getBatalhaoId())
                .orElseThrow(() -> new IllegalArgumentException("Batalhão não encontrado"));
        Notification notification = new Notification();
        notification.setTitulo(dto.getTitulo());
        notification.setDescricao(dto.getDescricao());
        notification.setBatalhao(batalhao);
        return notificationRepository.save(notification);
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

    public List<Notification> getByBatalhao(Long batalhaoId) {
        return notificationRepository.findByBatalhaoId(batalhaoId);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.getById(id);
    }

}
