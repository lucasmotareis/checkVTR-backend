package pmto._bpm.viaturas.viaturas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ViaturaComBadgeDTO(
        Long id,
        String prefixo,
        String modelo,
        String placa,
        boolean manutencao,
        Integer kmAtual,
        Integer kmRevisao,
        long checklistsNaoVistos
) {}
