package pmto._bpm.viaturas.viaturas.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class ViaturaPendenciaResponseDTO {

    private Long id;
    private Long viaturaId;
    private String prefixo;

    private Long problemaId;
    private String categoria;
    private String problemaNome;

    private String status;
    private Integer qtdRelatos;

    private Instant primeiraOcorrenciaEm;
    private Instant ultimaOcorrenciaEm;

    private String ultimaObservacao;

    private Long primeiroChecklistId;
    private Long ultimoChecklistId;

    private String resolvidoPor;
    private Instant resolvidoEm;
    private String observacaoResolucao;
}
