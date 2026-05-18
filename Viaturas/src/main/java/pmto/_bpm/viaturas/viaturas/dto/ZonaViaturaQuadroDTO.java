package pmto._bpm.viaturas.viaturas.dto;

import java.util.List;

public record ZonaViaturaQuadroDTO(
        Long id,
        String nome,
        String label,
        boolean virtual,
        List<ZonaViaturaCidadeQuadroDTO> cidades,
        List<ViaturaQuadroItemDTO> viaturas
) {}
