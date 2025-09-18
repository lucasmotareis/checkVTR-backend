package pmto._bpm.Viaturas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "item_checklist")
public class ItemCheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tipoProblema;
    private String item;
    private String observacao;

    public ItemCheckList() {
    }

    public ItemCheckList(CheckList checklist, String observacao, String item, String tipoProblema, Long id) {
        this.checklist = checklist;
        this.observacao = observacao;
        this.item = item;
        this.tipoProblema = tipoProblema;
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "checklist_id")
    @JsonBackReference

    private CheckList checklist;


    public CheckList getChecklist() {
        return checklist;
    }

    public void setChecklist(CheckList checklist) {
        this.checklist = checklist;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }


    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getTipoProblema() {
        return tipoProblema;
    }

    public void setTipoProblema(String tipoProblema) {
        this.tipoProblema = tipoProblema;
    }
}
