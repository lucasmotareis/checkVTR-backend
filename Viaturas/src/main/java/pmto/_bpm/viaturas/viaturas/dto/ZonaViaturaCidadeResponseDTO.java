package pmto._bpm.viaturas.viaturas.dto;

public record ZonaViaturaCidadeResponseDTO(
        Long id,
        Long zonaId,
        String cidade,
        Integer ordem,
        String label
) {}
