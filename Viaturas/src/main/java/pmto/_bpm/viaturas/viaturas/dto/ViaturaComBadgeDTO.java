package pmto._bpm.viaturas.viaturas.dto;

public record ViaturaComBadgeDTO(
        Long id,
        String prefixo,
        String modelo,
        String placa,
        boolean manutencao,
        Integer kmAtual,
        Integer kmRevisao,
        Integer combustivelAtualPercentual,
        long checklistsNaoVistos
) {}
