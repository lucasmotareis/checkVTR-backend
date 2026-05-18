package pmto._bpm.viaturas.viaturas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    private int kmAtual;
    private int kmRevisao;
    private boolean manutencao;

    @Column(name = "combustivel_atual_percentual")
    private Integer combustivelAtualPercentual;

    @OneToMany(mappedBy = "viatura")
    private List<ViaturaPendencia> pendencias = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "batalhao_id")
    private Batalhao batalhao;

    @ManyToOne
    @JoinColumn(name = "zona_id")
    private ZonaViatura zona;

    @ManyToOne
    @JoinColumn(name = "zona_cidade_id")
    private ZonaViaturaCidade zonaCidade;



    public Viatura() {
    }

    public Viatura(Batalhao batalhao, Boolean manutencao, int kmRevisao, int kmAtual, String prefixo, String placa, String modelo, Long id) {
        this.kmRevisao = kmRevisao;
        this.kmAtual = kmAtual;
        this.prefixo = prefixo;
        this.placa = placa;
        this.modelo = modelo;
        this.id = id;
        this.batalhao = batalhao;
        this.manutencao = manutencao;
    }



}
