package pmto._bpm.viaturas.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationDTO {

    @NotBlank(message = "O titulo e obrigatorio.")
    @Size(max = 80, message = "O titulo deve ter no maximo 80 caracteres.")
    private String titulo;

    @NotBlank(message = "A descricao e obrigatoria.")
    @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres.")
    private String mensagem;

    private Long batalhaoId;

    public CreateNotificationDTO() {
    }

    public CreateNotificationDTO(String titulo, String mensagem, Long batalhaoId) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.batalhaoId = batalhaoId;
    }
}
