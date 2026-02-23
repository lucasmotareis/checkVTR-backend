package pmto._bpm.viaturas.viaturas.dto;

import pmto._bpm.viaturas.batalhao.model.Batalhao;

public record ViaturaByIdDTO(
        Long id,
        String prefixo,
        String placa,
        String modelo,
        int kmAtual,
        int kmRevisao,
        boolean manutencao,
        Batalhao batalhao

) {}


