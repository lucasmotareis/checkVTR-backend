package pmto._bpm.viaturas.viaturas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ZonaViaturaCidadeDTO {

    @NotBlank(message = "A cidade e obrigatoria.")
    private String cidade;
}
