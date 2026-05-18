package pmto._bpm.viaturas.viaturas.dto;

import java.util.List;

public record ZonaViaturaCidadeQuadroDTO(
        Long id,
        String cidade,
        String label,
        boolean virtual,
        List<ViaturaQuadroItemDTO> viaturas
) {}
