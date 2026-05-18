package pmto._bpm.viaturas.viaturas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import pmto._bpm.viaturas.batalhao.model.Batalhao;

@Entity
@Table(name = "zona_viatura")
@Getter
@Setter
public class ZonaViatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer ordem;

    @ManyToOne
    @JoinColumn(name = "batalhao_id", nullable = false)
    private Batalhao batalhao;

    @OneToMany(mappedBy = "zona")
    private List<Viatura> viaturas = new ArrayList<>();

    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZonaViaturaCidade> cidades = new ArrayList<>();
}
