package pmto._bpm.viaturas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="viatura")
@Getter
@Setter
public class Viatura {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String modelo;
    private String placa;
    private String prefixo;
    private int km_atual;
    private int km_revisao;
    private boolean manutencao;

    @ManyToOne
    @JoinColumn(name = "batalhao_id")
    private Batalhao batalhao;



    public Viatura() {
    }

    public Viatura(Batalhao batalhao,Boolean manutencao, int km_revisao, int km_atual, String prefixo, String placa, String modelo, Long id) {
        this.km_revisao = km_revisao;
        this.km_atual = km_atual;
        this.prefixo = prefixo;
        this.placa = placa;
        this.modelo = modelo;
        this.id = id;
        this.batalhao = batalhao;
        this.manutencao = manutencao;
    }



}
