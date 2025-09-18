package pmto._bpm.Viaturas.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Viatura")
public class Viatura {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;
    private String prefixo;
    private int km_atual;
    private int km_revisao;

    @OneToMany(mappedBy = "viatura", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CheckList> checklists = new ArrayList<>();


    public Viatura() {
    }

    public Viatura(Long id, String placa, int km_atual, String prefixo, int km_revisao) {
        this.id = id;
        this.placa = placa;
        this.km_atual = km_atual;
        this.prefixo = prefixo;
        this.km_revisao = km_revisao;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getKm_atual() {
        return km_atual;
    }

    public void setKm_atual(int km_atual) {
        this.km_atual = km_atual;
    }

    public int getKm_revisao() {
        return km_revisao;
    }

    public void setKm_revisao(int km_revisao) {
        this.km_revisao = km_revisao;
    }

    public List<CheckList> getChecklists() {
        return checklists;
    }

    public void setChecklists(List<CheckList> checklists) {
        this.checklists = checklists;
    }
}
