package pmto._bpm.viaturas.notifications.dto;

import java.time.Instant;
import java.time.OffsetDateTime;

public record NotificacaoItemDTO(
        Long id,
        String titulo,
        String mensagem,
        OffsetDateTime dataCriacao,
        boolean lida
) {}
