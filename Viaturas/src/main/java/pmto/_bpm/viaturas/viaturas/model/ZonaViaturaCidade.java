package pmto._bpm.viaturas.viaturas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "zona_viatura_cidade")
@Getter
@Setter
public class ZonaViaturaCidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cidade;

    private Integer ordem;

    @ManyToOne
    @JoinColumn(name = "zona_id", nullable = false)
    private ZonaViatura zona;

    @OneToMany(mappedBy = "zonaCidade")
    private List<Viatura> viaturas = new ArrayList<>();
}
