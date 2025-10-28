package pmto._bpm.viaturas.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationDTO {

    @NotBlank(message = "O título é obrigatório.")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    private String descricao;

    private Long batalhaoId;
    // Construtor padrão
    public NotificationDTO() {
    }

    // Construtor com argumentos
    public NotificationDTO(String titulo, String descricao, Long batalhaoId) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.batalhaoId = batalhaoId;
    }

    public Long getBatalhaoId() {
        return batalhaoId;
    }

    public void setBatalhaoId(Long batalhaoId) {
        this.batalhaoId = batalhaoId;
    }

    // Getters e Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
