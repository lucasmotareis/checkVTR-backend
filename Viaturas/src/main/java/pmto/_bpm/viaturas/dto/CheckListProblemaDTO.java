package pmto._bpm.viaturas.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckListProblemaDTO {

    private Long problemaId;
    private String problemaNome;
    private String categoria;
    private String observacao;


}
