package pmto._bpm.Viaturas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ViaturaDTO {

    @NotBlank(message = "A placa não pode estar vazia.")
    private String placa;

    @NotBlank(message = "O modelo não pode estar vazio.")
    private String modelo;


    @NotBlank(message = "O prefixo é obrigatório.")
    private String prefixo;

    @NotNull(message = "O KM atual é obrigatório.")
    @Min(value = 0, message = "KM atual não pode ser negativo.")
    private Integer kmAtual;

    @NotNull(message = "O KM de revisão é obrigatório.")
    @Min(value = 0, message = "KM de revisão não pode ser negativo.")
    private Integer kmRevisao;

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getPrefixo() {
        return prefixo;
    }

    public void setPrefixo(String prefixo) {
        this.prefixo = prefixo;
    }

    public Integer getKmAtual() {
        return kmAtual;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }

    public Integer getKmRevisao() {
        return kmRevisao;
    }

    public void setKmRevisao(Integer kmRevisao) {
        this.kmRevisao = kmRevisao;
    }
}
