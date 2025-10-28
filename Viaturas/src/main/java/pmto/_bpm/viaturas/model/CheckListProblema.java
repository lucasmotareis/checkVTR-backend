package pmto._bpm.viaturas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;


@Entity
@Table(name = "checklist_problema")
public class CheckListProblema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "checklist_id")
    @JsonBackReference
    private CheckList checklist;

    @ManyToOne
    @JoinColumn(name = "problema_id")
    private Problema problema;

    private String observacao;

    public CheckList getChecklist() {
        return checklist;
    }


    public void setChecklist(CheckList checklist) {
        this.checklist = checklist;
    }

    public Problema getProblema() {
        return problema;
    }

    public void setProblema(Problema problema) {
        this.problema = problema;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
