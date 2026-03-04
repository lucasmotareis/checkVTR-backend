package pmto._bpm.viaturas.viaturas.dto;

public record ViaturaByIdDTO(
        Long id,
        String prefixo,
        String placa,
        String modelo,
        int kmAtual,
        int kmRevisao,
        boolean manutencao,
        Long batalhaoId,
        String batalhaoNome

) {}


