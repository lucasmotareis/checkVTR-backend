package pmto._bpm.viaturas.viaturas.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReordenarZonaViaturaDTO {

    @NotEmpty(message = "A lista de zonas e obrigatoria.")
    private List<@NotNull(message = "Os IDs das zonas sao obrigatorios.") Long> zonaIds;
}
