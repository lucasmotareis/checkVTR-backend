package pmto._bpm.Viaturas.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CheckListDTO {

    @NotNull(message = "ID da viatura é obrigatório.")
    private Long viaturaId;

    @Min(value = 1, message = "KM deve ser maior que zero")
    private Integer KmAtual;

    private List<String> imagens = new ArrayList<>();

    public List<String> getImagens() {
        return imagens;
    }

    public void setImagens(List<String> imagens) {
        this.imagens = imagens;
    }

    public Integer getKmAtual() {
        return this.KmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.KmAtual = kmAtual;
    }

    @NotNull(message = "Lista de itens não pode ser nula.")
    private List<ItemCheckListDTO> itens;

    public Long getViaturaId() {
        return this.viaturaId;
    }

    public void setViaturaId(Long viaturaId) {
        this.viaturaId = viaturaId;
    }

    public List<ItemCheckListDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemCheckListDTO> itens) {
        this.itens = itens;
    }
}
