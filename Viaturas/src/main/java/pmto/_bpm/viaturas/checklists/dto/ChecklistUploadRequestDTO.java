package pmto._bpm.viaturas.checklists.dto;

public class ChecklistUploadRequestDTO {

    private ChecklistUploadMetadata metadata;
    private int quantidade; // Quantidade de imagens que serão enviadas

    public ChecklistUploadMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ChecklistUploadMetadata metadata) {
        this.metadata = metadata;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
