package pmto._bpm.viaturas.dto;

public class CheckListProblemaDTO {

    private Long problemaId;
    private String observacao;

    // Getters e Setters
    public Long getProblemaId() {
        return problemaId;
    }

    public void setProblemaId(Long problemaId) {
        this.problemaId = problemaId;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
