package pmto._bpm.viaturas.dto;
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
    private List<CheckListProblemaDTO> problemas;


    @Min(value = 1, message = "KM deve ser maior que zero")
    private Integer kmAtual;

    @Min(value = 1, message = "KM deve ser maior que zero")
    private Integer kmRevisao;

    private List<String> imagens = new ArrayList<>();

}
