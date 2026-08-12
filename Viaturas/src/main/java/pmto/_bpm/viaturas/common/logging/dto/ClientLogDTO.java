package pmto._bpm.viaturas.common.logging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ClientLogDTO {

    @NotBlank(message = "Origem do log e obrigatoria.")
    @Size(max = 80, message = "Origem do log deve ter no maximo 80 caracteres.")
    private String source;

    @NotBlank(message = "Evento do log e obrigatorio.")
    @Size(max = 120, message = "Evento do log deve ter no maximo 120 caracteres.")
    private String event;

    @Size(max = 80, message = "Nivel do log deve ter no maximo 80 caracteres.")
    private String level = "info";

    @Size(max = 120, message = "ID de submissao deve ter no maximo 120 caracteres.")
    private String clientSubmissionId;

    private Map<String, Object> data = new LinkedHashMap<>();
}
