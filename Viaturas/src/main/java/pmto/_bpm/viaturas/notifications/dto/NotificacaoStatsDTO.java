package pmto._bpm.viaturas.notifications.dto;

import java.time.Instant;

public record NotificacaoStatsDTO(
        long total,
        long today,
        long thisWeek,
        long thisMonth,
        Instant lastCreatedAt
) {


}
