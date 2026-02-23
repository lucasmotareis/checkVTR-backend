package pmto._bpm.viaturas.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateNotificationDTO {

    @NotBlank(message = "O título é obrigatório.")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
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
