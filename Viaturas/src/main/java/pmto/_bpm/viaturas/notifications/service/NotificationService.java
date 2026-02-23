package pmto._bpm.viaturas.notifications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.notifications.dto.NotificacaoItemDTO;
import pmto._bpm.viaturas.notifications.dto.NotificacaoStatsDTO;
import pmto._bpm.viaturas.notifications.model.Notification;
import pmto._bpm.viaturas.notifications.dto.CreateNotificationDTO;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.notifications.repository.NotificationRepository;
import pmto._bpm.viaturas.users.model.User;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Autowired
    private BatalhaoRepository batalhaoRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    public Notification criar(CreateNotificationDTO dto) {
        Batalhao batalhao = batalhaoRepository.findById(dto.getBatalhaoId())
                .orElseThrow(() -> new IllegalArgumentException("Batalhão não encontrado"));
        Notification notification = new Notification();
        notification.setTitulo(dto.getTitulo());
        notification.setMensagem(dto.getMensagem());
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

    public Notification atualizar (Long id, CreateNotificationDTO dto) {
        Notification existente = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada com ID: " + id));
        existente.setTitulo(dto.getTitulo());
        existente.setMensagem(dto.getMensagem());
        return notificationRepository.save(existente);
    }



    public Notification getNotificationById(Long id) {
        return notificationRepository.getReferenceById(id);
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
                        n.getBatalhao().getNome(),
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


    public NotificacaoStatsDTO getStats(Long batalhaoId) {

        ZoneId zone = ZoneId.of("America/Araguaina");

        Instant startOfDay = LocalDate.now(zone)
                .atStartOfDay(zone)
                .toInstant();

        Instant startOfWeek = LocalDate.now(zone)
                .with(DayOfWeek.MONDAY)
                .atStartOfDay(zone)
                .toInstant();

        Instant startOfMonth = LocalDate.now(zone)
                .withDayOfMonth(1)
                .atStartOfDay(zone)
                .toInstant();

        return notificationRepository.getStats(
                batalhaoId,
                startOfDay,
                startOfWeek,
                startOfMonth
        );
    }


}
