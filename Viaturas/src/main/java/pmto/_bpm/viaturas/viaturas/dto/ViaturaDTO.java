package pmto._bpm.viaturas.viaturas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ViaturaDTO {

    @NotBlank(message = "A placa nao pode estar vazia.")
    private String placa;

    @NotBlank(message = "O modelo nao pode estar vazio.")
    private String modelo;

    private boolean manutencao;

    @NotBlank(message = "O prefixo e obrigatorio.")
    private String prefixo;

    @NotNull(message = "O KM atual e obrigatorio.")
    @Min(value = 0, message = "KM atual nao pode ser negativo.")
    private Integer kmAtual;

    @NotNull(message = "O KM de revisao e obrigatorio.")
    @Min(value = 0, message = "KM de revisao nao pode ser negativo.")
    private Integer kmRevisao;

    @Min(value = 0, message = "Combustivel deve ser no minimo 0.")
    @Max(value = 100, message = "Combustivel deve ser no maximo 100.")
    private Integer combustivelAtualPercentual;
}
