package pmto._bpm.viaturas.checklists.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckListProblemaRequestDTO {

    @NotNull(message = "problemId e obrigatorio.")
    private Long problemaId;
    private String observacao;


}
