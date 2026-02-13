package pmto._bpm.viaturas.notifications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.notifications.dto.NotificacaoItemDTO;
import pmto._bpm.viaturas.notifications.model.Notification;
import pmto._bpm.viaturas.notifications.dto.NotificationDTO;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.notifications.repository.NotificationRepository;
import pmto._bpm.viaturas.users.model.User;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

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
        notification.setMensagem(dto.getDescricao());
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
        existente.setMensagem(dto.getDescricao());
        return notificationRepository.save(existente);
    }



    public Notification getNotificationById(Long id) {
        return notificationRepository.getById(id);
    }


    public Page<NotificacaoItemDTO> listaNotificacaoPaginado(User user, Pageable pageable) {
        Instant lastSeen = user.getUltimaNotificacaoVista();
        ZoneId zone = ZoneId.of("America/Araguaina");

        return notificationRepository
                .findByBatalhaoId(user.getBatalhao().getId(), pageable)
                .map(n -> new NotificacaoItemDTO(
                        n.getId(),
                        n.getTitulo(),
                        n.getMensagem(),
                        n.getDataCriacao().atZone(zone).toOffsetDateTime(),
                        lastSeen != null && !n.getDataCriacao().isAfter(lastSeen)
                ));
    }



    public long notificacoesNaoLidas(User user) {
        Long batalhaoId = user.getBatalhao().getId();
        Instant lastSeen = user.getUltimaNotificacaoVista();

        if (lastSeen == null) {
            return notificationRepository.countByBatalhaoId(batalhaoId);
        }
        return notificationRepository.countByBatalhaoIdAndDataCriacaoAfter(batalhaoId, lastSeen);
    }



}
