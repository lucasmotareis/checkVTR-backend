package pmto._bpm.viaturas.notifications.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pmto._bpm.viaturas.batalhao.model.Batalhao;
import pmto._bpm.viaturas.batalhao.repository.BatalhaoRepository;
import pmto._bpm.viaturas.notifications.dto.NotificationResponseDTO;
import pmto._bpm.viaturas.notifications.model.Notification;
import pmto._bpm.viaturas.notifications.repository.NotificationRepository;
import pmto._bpm.viaturas.users.model.User;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BatalhaoRepository batalhaoRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void notificacoesNaoLidasShouldUseTotalCountWhenLastSeenIsNull() {
        User user = new User();
        Batalhao batalhao = new Batalhao();
        batalhao.setId(10L);
        user.setBatalhao(batalhao);
        user.setUltimaNotificacaoVista(null);

        when(notificationRepository.countByBatalhaoId(10L)).thenReturn(7L);

        long total = notificationService.notificacoesNaoLidas(user);

        assertEquals(7L, total);
        verify(notificationRepository).countByBatalhaoId(10L);
        verify(notificationRepository, never()).countByBatalhaoIdAndDataCriacaoAfter(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void notificacoesNaoLidasShouldUseAfterDateWhenLastSeenExists() {
        User user = new User();
        Batalhao batalhao = new Batalhao();
        batalhao.setId(20L);
        user.setBatalhao(batalhao);
        Instant lastSeen = Instant.parse("2026-01-01T00:00:00Z");
        user.setUltimaNotificacaoVista(lastSeen);

        when(notificationRepository.countByBatalhaoIdAndDataCriacaoAfter(20L, lastSeen)).thenReturn(3L);

        long total = notificationService.notificacoesNaoLidas(user);

        assertEquals(3L, total);
        verify(notificationRepository).countByBatalhaoIdAndDataCriacaoAfter(20L, lastSeen);
        verify(notificationRepository, never()).countByBatalhaoId(20L);
    }

    @Test
    void toResponseDTOShouldMapNotificationFields() {
        Batalhao batalhao = new Batalhao();
        batalhao.setId(1L);
        batalhao.setNome("8 BPM");

        Notification notification = new Notification();
        notification.setId(99L);
        notification.setTitulo("Alerta");
        notification.setMensagem("Mensagem teste");
        notification.setBatalhao(batalhao);
        notification.setDataCriacao(Instant.parse("2026-02-01T12:00:00Z"));

        NotificationResponseDTO dto = notificationService.toResponseDTO(notification);

        assertEquals(99L, dto.id());
        assertEquals("Alerta", dto.titulo());
        assertEquals("Mensagem teste", dto.mensagem());
        assertEquals(1L, dto.batalhaoId());
        assertEquals("8 BPM", dto.nomeBatalhao());
        assertNotNull(dto.dataCriacao());
    }
}

