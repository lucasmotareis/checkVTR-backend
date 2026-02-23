package pmto._bpm.viaturas.analytics.dto;

public record DashboardDTO(
        long viaturasEmManutencao,
        long viaturasDisponiveis,
        long checklistsNaoVistos
) {}
