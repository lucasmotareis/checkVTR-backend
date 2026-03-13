package pmto._bpm.viaturas.checklists.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckListProblemaResponseDTO {
    private Long problemaId;
    private String categoria;
    private String problemaNome;
    private String observacao;
}