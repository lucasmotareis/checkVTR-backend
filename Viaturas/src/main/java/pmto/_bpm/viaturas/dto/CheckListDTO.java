package pmto._bpm.viaturas.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CheckListDTO {

    @NotNull(message = "ID da viatura é obrigatório.")
    private Long viaturaId;

    @NotNull(message = "Lista de itens não pode ser nula.")
    private List<CheckListProblemaDTO> problemas;


    @Min(value = 1, message = "KM deve ser maior que zero")
    private Integer kmAtual;

    private List<String> imagens = new ArrayList<>();

    public List<String> getImagens() {
        return imagens;
    }


    public void setViaturaId(Long viaturaId) {
        this.viaturaId = viaturaId;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public Integer getKmAtual() {
        return this.kmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }


    public Long getViaturaId() {
        return this.viaturaId;
    }

    public List<CheckListProblemaDTO> getProblemas() {
        return problemas;
    }

    public void setProblemas(List<CheckListProblemaDTO> problemas) {
        this.problemas = problemas;
    }
}
