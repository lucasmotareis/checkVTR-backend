package pmto._bpm.viaturas.viaturas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonaViaturaDTO {

    @NotBlank(message = "O nome da zona e obrigatorio.")
    private String nome;
}
