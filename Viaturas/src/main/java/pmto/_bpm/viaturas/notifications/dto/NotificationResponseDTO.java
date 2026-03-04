package pmto._bpm.viaturas.notifications.dto;

import java.time.OffsetDateTime;

public record NotificationResponseDTO(
        Long id,
        String titulo,
        String mensagem,
        OffsetDateTime dataCriacao,
        Long batalhaoId,
        String nomeBatalhao
) {}

