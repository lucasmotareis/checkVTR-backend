package pmto._bpm.viaturas.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "checklist_problema")
@Getter
@Setter
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

}
