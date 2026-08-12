package pmto._bpm.viaturas.checklists.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CheckListDTO {

    @NotNull(message = "ID da viatura é obrigatório.")
    private Long viaturaId;

    @NotNull(message = "Lista de itens não pode ser nula.")
    @Valid
    private List<CheckListProblemaRequestDTO> problemas = new ArrayList<>();


    @NotNull(message = "KM atual e obrigatorio.")
    @Min(value = 1, message = "KM deve ser maior que zero")
    private Integer kmAtual;

    @NotNull(message = "KM revisao e obrigatorio.")
    @Min(value = 1, message = "KM deve ser maior que zero")
    private Integer kmRevisao;

    @Min(value = 0, message = "Combustível deve ser no mínimo 0.")
    @Max(value = 100, message = "Combustível deve ser no máximo 100.")
    private Integer combustivelAtualPercentual;

    private List<String> imagens = new ArrayList<>();

    private String clientSubmissionId;

}
