package pmto._bpm.viaturas.viaturas.dto;

import pmto._bpm.viaturas.batalhao.model.Batalhao;

public record ViaturaByIdDTO(
        Long id,
        String prefixo,
        String placa,
        String modelo,
        int km_atual,
        int km_revisao,
        boolean manutencao,
        Batalhao batalhao

) {}


